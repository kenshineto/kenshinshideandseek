package cat.freya.khs.mod.internal

import cat.freya.khs.mod.ModServer
import cat.freya.khs.mod.mixin.MinecraftServerAccess
import cat.freya.khs.world.World
import cat.freya.khs.world.isMapSave
import com.google.common.collect.ImmutableList
import net.minecraft.core.Holder
import net.minecraft.core.RegistrationInfo
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Util
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.FixedBiomeSource
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.FlatLevelSource
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings
import net.minecraft.world.level.storage.DerivedLevelData

class LevelManager(modServer: ModServer) {
    private val levels: MutableMap<ResourceKey<Level>, ServerLevel> = mutableMapOf()

    private val server = modServer.inner
    private val serverAccess = server as MinecraftServerAccess

    private val biomes = modServer.getRegistry(Registries.BIOME)
    private val dimensionTypes = modServer.getRegistry(Registries.DIMENSION_TYPE)
    private val dimensions = modServer.getMappedRegistry(Registries.LEVEL_STEM)
    private val placedFeatures = modServer.getRegistry(Registries.PLACED_FEATURE)
    private val structureSets = modServer.getRegistry(Registries.STRUCTURE_SET)

    private val theOverworld = server.overworld()
    private val theNether = server.getLevel(Level.NETHER)
    private val theEnd = server.getLevel(Level.END)

    private val overworldGenerator = theOverworld.chunkSource.generator
    private val netherGenerator = theNether?.chunkSource?.generator ?: overworldGenerator
    private val endGenerator = theEnd?.chunkSource?.generator ?: overworldGenerator

    private val voidGenerator: ChunkGenerator
    private val flatGenerator: ChunkGenerator

    init {
        val voidBiome = biomes.getOrThrow(Biomes.THE_VOID)
        voidGenerator = VoidGenerator(FixedBiomeSource(voidBiome))

        val flatGenSettings = FlatLevelGeneratorSettings.getDefault(biomes, structureSets, placedFeatures)
        flatGenerator = FlatLevelSource(flatGenSettings)
    }

    fun create(key: ResourceKey<Level>, type: World.Type): ServerLevel {
        val dimensionType = getDimensionType(type)
        val generator = getGenerator(key, type)
        val levelStem = LevelStem(dimensionType, generator)
        val seed = theOverworld.seed
        val worldData = server.worldData
        val levelData = DerivedLevelData(worldData, worldData.overworldData())

        RemoveFromRegistry.thaw(dimensions).use {
            val id = key.identifier()
            if (!dimensions.containsKey(id)) {
                val levelStemKey = ResourceKey.create(Registries.LEVEL_STEM, id)
                dimensions.register(levelStemKey, levelStem, RegistrationInfo.BUILT_IN)
            }
        }

        val level =
            ServerLevel(
                server,
                Util.backgroundExecutor(),
                serverAccess.getStorageSource(),
                levelData,
                key,
                levelStem,
                false,
                BiomeManager.obfuscateSeed(seed),
                ImmutableList.of(),
                true,
            )

        val worldName = key.identifier().toString()
        if (isMapSave(worldName)) {
            level.noSave = true
        }

        add(level)
        level.tick { true }

        server.playerList.addWorldborderListener(level)
        level.worldBorder.setAbsoluteMaxSize(server.absoluteMaxWorldSize)

        return level
    }

    fun add(level: ServerLevel) {
        val key = level.dimension()
        levels[key] = level
        serverAccess.getLevels().put(key, level)
    }

    fun remove(level: ServerLevel) {
        val key = level.dimension()
        levels.remove(key)
        serverAccess.getLevels().remove(key, level)
        RemoveFromRegistry.remove(dimensions, key.identifier())
    }

    fun get(key: ResourceKey<Level>): ServerLevel? {
        return levels.get(key)
    }

    private fun getDimensionTypeKey(type: World.Type): ResourceKey<DimensionType> {
        when (type) {
            World.Type.NETHER -> return BuiltinDimensionTypes.NETHER
            World.Type.END -> return BuiltinDimensionTypes.END
            else -> return BuiltinDimensionTypes.OVERWORLD
        }
    }

    private fun getDimensionType(type: World.Type): Holder<DimensionType> {
        return dimensionTypes.getOrThrow(getDimensionTypeKey(type))
    }

    private fun getGenerator(key: ResourceKey<Level>, type: World.Type): ChunkGenerator {
        val worldName = key.identifier().toString()
        if (isMapSave(worldName)) {
            return voidGenerator
        }

        when (type) {
            World.Type.NETHER -> return netherGenerator
            World.Type.END -> return endGenerator
            World.Type.FLAT -> return flatGenerator
            else -> return overworldGenerator
        }
    }
}
