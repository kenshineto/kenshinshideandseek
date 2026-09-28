package cat.freya.khs.mod.internal

import cat.freya.khs.mod.ModServer
import cat.freya.khs.mod.mixin.MinecraftServerAccess
import cat.freya.khs.world.isMapSave
import com.google.common.collect.ImmutableList
import net.minecraft.core.Holder
import net.minecraft.core.RegistrationInfo
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Util
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.FixedBiomeSource
import net.minecraft.world.level.chunk.ChunkGenerator
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

    private val voidGenerator: ChunkGenerator
    private val flatGenerator: ChunkGenerator

    init {
        val voidBiome = biomes.getOrThrow(Biomes.THE_VOID)
        voidGenerator = VoidGenerator(FixedBiomeSource(voidBiome))

        val flatGenSettings = FlatLevelGeneratorSettings.getDefault(biomes, structureSets, placedFeatures)
        flatGenerator = FlatLevelSource(flatGenSettings)
    }

    fun create(id: Identifier, dimension: Identifier, isFlat: Boolean): ServerLevel? {
        val dimensionType = getDimensionType(dimension) ?: return null
        val generator = getGenerator(id, dimension, isFlat)
        val levelStem = LevelStem(dimensionType, generator)
        val seed = server.overworld().seed
        val worldData = server.worldData
        val levelData = DerivedLevelData(worldData, worldData.overworldData())

        RemoveFromRegistry.thaw(dimensions).use {
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
                ResourceKey.create(Registries.DIMENSION, id),
                levelStem,
                false,
                BiomeManager.obfuscateSeed(seed),
                ImmutableList.of(),
                true,
            )

        if (isMapSave(id.toString())) {
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

    private fun getDimensionType(dimension: Identifier): Holder<DimensionType>? {
        val key = ResourceKey.create(Registries.DIMENSION_TYPE, dimension)
        return dimensionTypes.get(key).orElse(null)
    }

    private fun getGenerator(id: Identifier, dimension: Identifier, isFlat: Boolean): ChunkGenerator {
        if (isMapSave(id.toString())) {
            return voidGenerator
        }

        if (isFlat) {
            return flatGenerator
        }

        val key = ResourceKey.create(Registries.DIMENSION, dimension)
        val level = server.getLevel(key) ?: server.overworld()
        return level.chunkSource.generator
    }
}
