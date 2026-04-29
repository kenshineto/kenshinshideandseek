package cat.freya.khs.mod

import cat.freya.khs.mod.ModWorld
import cat.freya.khs.mod.mixin.MixinMinecraftServer
import cat.freya.khs.world.AbstractWorld
import cat.freya.khs.world.Location
import cat.freya.khs.world.Position
import cat.freya.khs.world.World
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.WorldGenRegion
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.level.NoiseColumn
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.BiomeSource
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.FixedBiomeSource
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.FlatLevelSource
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.RandomState
import net.minecraft.world.level.levelgen.blending.Blender
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings
import net.minecraft.world.level.storage.DerivedLevelData
import net.minecraft.world.level.storage.LevelStorageSource
import java.util.concurrent.CompletableFuture

class VoidGenerator(biomeSource: BiomeSource) : ChunkGenerator(biomeSource) {
    override fun codec(): MapCodec<ChunkGenerator> {
        val biomeCodec = BiomeSource.CODEC.fieldOf("biome_source")

        return RecordCodecBuilder.mapCodec {
            it
                .group(biomeCodec.forGetter { it.biomeSource })
                .apply(it) { VoidGenerator(it) }
        }
    }

    override fun applyCarvers(region: WorldGenRegion, seed: Long, randomState: RandomState, biomeManager: BiomeManager, structureManager: StructureManager, chunk: ChunkAccess) {
        // no carving
    }

    override fun buildSurface(level: WorldGenRegion, structureManager: StructureManager, randomState: RandomState, protoChunk: ChunkAccess) {
        // no surface
    }

    override fun spawnOriginalMobs(worldGenRegion: WorldGenRegion) {
        // no mobs
    }

    override fun getGenDepth(): Int {
        return 384
    }

    override fun fillFromNoise(blender: Blender, randomState: RandomState, structureManager: StructureManager, centerChunk: ChunkAccess): CompletableFuture<ChunkAccess> {
        return CompletableFuture.completedFuture(centerChunk)
    }

    override fun getSeaLevel(): Int {
        return 0
    }

    override fun getMinY(): Int {
        return -64
    }

    override fun getBaseHeight(x: Int, z: Int, type: Heightmap.Types, heightAccessor: LevelHeightAccessor, randomState: RandomState): Int {
        return heightAccessor.minY
    }

    override fun getBaseColumn(x: Int, z: Int, heightAccessor: LevelHeightAccessor, randomState: RandomState): NoiseColumn {
        return NoiseColumn(heightAccessor.minY, emptyArray())
    }

    override fun addDebugScreenInfo(result: MutableList<String>, randomState: RandomState, feetPos: BlockPos) {
        // no debug info needed
    }
}

class ModWorldBorder(val level: ServerLevel) : World.Border {
    private val border = level.worldBorder

    override val x: Double
        get() = border.centerX

    override val z: Double
        get() = border.centerZ

    override val size: Double
        get() = border.size

    override fun move(newX: Double, newZ: Double, newSize: ULong, delay: ULong) {
        border.setCenter(newX, newZ)
        move(newSize, delay)
    }

    override fun move(newSize: ULong, delay: ULong) {
        border.lerpSizeBetween(size, newSize.toDouble(), delay.toLong(), level.gameTime)
    }
}

class ModWorldLoader(val mod: KhsMod, override val name: String) : World.AbstractLoader(name, name, mod.server.getWorldContainer()) {
    override fun load(): ModWorld? {
        val key = ModWorld.parseKey(name) ?: return null
        val level =
            mod.server.inner.getLevel(key)
                ?: ModWorld.createLevel(mod, name, World.Type.NORMAL)
                ?: return null
        return ModWorld(mod, level)
    }

    override fun unload() {
        val key = ModWorld.parseKey(name) ?: return
        val mixinServer = (mod.server.inner) as MixinMinecraftServer
        mixinServer.removeLevel(key, !isMapSave)
    }
}

class ModWorld(val mod: KhsMod, val inner: ServerLevel) : AbstractWorld(mod.shim) {
    override val name = inner.toString() // toString calls serverLevelData.levelName

    override val type: World.Type = getTypeImpl()

    private fun getTypeImpl(): World.Type {
        if (inner.isFlat) return World.Type.FLAT

        val dim = inner.dimension()
        return when (dim) {
            Level.OVERWORLD -> World.Type.NORMAL
            Level.NETHER -> World.Type.NETHER
            Level.END -> World.Type.END
            else -> World.Type.UNKNOWN
        }
    }

    override val border = ModWorldBorder(inner)

    override val loader = ModWorldLoader(mod, name)

    override fun getSpawn(): Location {
        val pos = inner.respawnData.pos()
        return Location(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), name)
    }

    override fun playSound(position: Position, sound: String, volume: Double, pitch: Double) {
        val id = Identifier.tryParse(sound) ?: return
        val holder = BuiltInRegistries.SOUND_EVENT.get(id).orElse(null) ?: return

        inner.playSound(null, position.x, position.y, position.z, holder, SoundSource.AMBIENT, volume.toFloat(), pitch.toFloat())
    }

    companion object {
        fun createLevel(mod: KhsMod, worldName: String, type: World.Type): ServerLevel? {
            val server = mod.server.inner
            val loader = ModWorldLoader(mod, worldName)

            val key = parseKey(worldName) ?: return null
            if (key.identifier().namespace != KhsMod.ID) return null

            val generator =
                if (loader.isMapSave) {
                    voidGenerator(mod)
                } else {
                    getGenerator(mod, type) ?: return null
                }

            val dimension = getDimension(mod, type)

            // get world "session"
            val levelStorage = LevelStorageSource.createDefault(mod.server.getWorldContainer())
            val session = levelStorage.createAccess(worldName)

            val levelStem = LevelStem(dimension, generator)

            val levelData = DerivedLevelData(server.worldData, server.worldData.overworldData())

            val level =
                ServerLevel(
                    server,
                    server, // executor
                    session,
                    levelData,
                    key, // dimension
                    levelStem,
                    false, // isDebug
                    server.overworld().seed,
                    listOf(),
                    false,
                )

            // insert into minecraft server
            val mixinServer = server as MixinMinecraftServer
            mixinServer.initLevel(level)

            return level
        }

        private fun getGenerator(mod: KhsMod, type: World.Type): ChunkGenerator? {
            val server = mod.server.inner
            return when (type) {
                World.Type.NORMAL -> server.overworld().chunkSource.generator
                World.Type.NETHER -> server.getLevel(Level.NETHER)?.chunkSource?.generator
                World.Type.END -> server.getLevel(Level.END)?.chunkSource?.generator
                World.Type.FLAT -> flatGenerator(mod)
                else -> null
            }
        }

        private fun getDimension(mod: KhsMod, type: World.Type): Holder<DimensionType> {
            val dimensionType =
                when (type) {
                    World.Type.NETHER -> BuiltinDimensionTypes.NETHER
                    World.Type.END -> BuiltinDimensionTypes.END
                    else -> BuiltinDimensionTypes.OVERWORLD
                }

            return mod.server.inner
                .registryAccess()
                .lookupOrThrow(Registries.DIMENSION_TYPE)
                .getOrThrow(dimensionType)
        }

        private fun flatGenerator(mod: KhsMod): ChunkGenerator {
            val registries = mod.server.inner.registryAccess()
            val biomes = registries.lookupOrThrow(Registries.BIOME)
            val structureSets = registries.lookupOrThrow(Registries.STRUCTURE_SET)
            val placedFeatures = registries.lookupOrThrow(Registries.PLACED_FEATURE)
            val settings = FlatLevelGeneratorSettings.getDefault(biomes, structureSets, placedFeatures)
            return FlatLevelSource(settings)
        }

        private fun voidGenerator(mod: KhsMod): ChunkGenerator {
            val registries = mod.server.inner.registryAccess()
            val biomes = registries.lookupOrThrow(Registries.BIOME)
            val biome = biomes.getOrThrow(Biomes.THE_VOID)
            return VoidGenerator(FixedBiomeSource(biome))
        }

        fun parseKey(worldName: String): ResourceKey<Level>? {
            val id = Identifier.tryParse(worldName) ?: return null
            return ResourceKey.create(Registries.DIMENSION, id)
        }
    }
}
