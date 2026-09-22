package cat.freya.khs.mod

import cat.freya.khs.world.Location
import cat.freya.khs.world.Position
import cat.freya.khs.world.World
import cat.freya.khs.world.isMapSave
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.concurrent.CompletableFuture
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
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.BiomeSource
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.FixedBiomeSource
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.levelgen.FlatLevelSource
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.RandomState
import net.minecraft.world.level.levelgen.blending.Blender
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings

class VoidGenerator(biomeSource: BiomeSource) : ChunkGenerator(biomeSource) {
    override fun codec(): MapCodec<ChunkGenerator> {
        val biomeCodec = BiomeSource.CODEC.fieldOf("biome_source")

        return RecordCodecBuilder.mapCodec {
            it.group(biomeCodec.forGetter { it.biomeSource }).apply(it) { VoidGenerator(it) }
        }
    }

    override fun spawnOriginalMobs(worldGenRegion: WorldGenRegion) {
        // no mobs
    }

    override fun getGenDepth(): Int {
        return 384
    }

    override fun getSeaLevel(): Int {
        return 0
    }

    override fun getMinY(): Int {
        return -64
    }

    override fun getBaseHeight(
        x: Int,
        z: Int,
        type: Heightmap.Types,
        heightAccessor: LevelHeightAccessor,
        randomState: RandomState,
    ): Int {
        return heightAccessor.minY
    }

    override fun getBaseColumn(
        x: Int,
        z: Int,
        heightAccessor: LevelHeightAccessor,
        randomState: RandomState,
    ): NoiseColumn {
        return NoiseColumn(heightAccessor.minY, emptyArray())
    }

    override fun buildTerrain(
        chunk: ChunkAccess,
        blender: Blender,
        randomState: RandomState,
        structureManager: StructureManager,
        biomeManager: BiomeManager,
        carverBiomeRegion: WorldGenRegion?,
        possibleBiomes: MutableSet<Holder<Biome>>,
    ): CompletableFuture<ChunkAccess> {
        return CompletableFuture.completedFuture(chunk)
    }

    override fun addDebugScreenInfo(
        result: MutableList<String>,
        randomState: RandomState,
        feetPos: BlockPos,
        samplerContext: SamplerContext,
    ) {
        // do nothing
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

class ModWorld(val mod: KhsMod, val inner: ServerLevel) : World {
    override val name = inner.dimension().identifier().toString()

    override val type: World.Type = getKhsWorldType()

    private fun getDimensionType(): ResourceKey<DimensionType>? {
        val registry = mod.server.inner.registryAccess().lookupOrThrow(Registries.DIMENSION_TYPE)
        return registry.getResourceKey(inner.dimensionType()).orElse(null)
    }

    private fun getKhsWorldType(): World.Type {
        if (inner.isFlat) return World.Type.FLAT

        return when (getDimensionType()) {
            BuiltinDimensionTypes.OVERWORLD -> World.Type.NORMAL
            BuiltinDimensionTypes.NETHER -> World.Type.NETHER
            BuiltinDimensionTypes.END -> World.Type.END
            else -> World.Type.UNKNOWN
        }
    }

    override val border = ModWorldBorder(inner)

    override fun getSpawn(): Location {
        val pos = inner.respawnData.pos()
        return Location(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), name, 0f, 0f)
    }

    override fun playSound(position: Position, sound: String, volume: Double, pitch: Double) {
        val id = Identifier.tryParse(sound) ?: return
        val holder = BuiltInRegistries.SOUND_EVENT.get(id).orElse(null) ?: return

        inner.playSound(
            null,
            position.x,
            position.y,
            position.z,
            holder,
            SoundSource.AMBIENT,
            volume.toFloat(),
            pitch.toFloat(),
        )
    }

    override fun unload() {
        val key = inner.dimension()
        mod.server.unregisterLevel(key)

        inner.players().forEach { modPlayer ->
            val player = ModPlayer(mod, modPlayer)
            player.teleport(mod.khs.config.exit)
        }

        mod.platform.unloadLevel(key)
    }

    override fun save() {
        inner.chunkSource.dataStorage.saveAndJoin()
        inner.save(null, true, inner.noSave)
    }

    companion object {
        fun createLevel(mod: KhsMod, worldName: String, type: World.Type): ServerLevel? {
            val id = Identifier.tryParse(worldName)
            if (id == null) {
                mod.shim.logger.warning("invalid world name: ${worldName}")
                return null
            }

            val key = ResourceKey.create(Registries.DIMENSION, id)
            val dimension = getDimension(type)
            val generator = getGenerator(mod, id, type)
            val level = mod.platform.createLevel(key, dimension, generator)
            if (level == null) {
                mod.shim.logger.warning("failed to load level: ${worldName} as ${type}")
                return null
            }

            if (isMapSave(id.path)) {
                level.noSave = true
            }

            mod.server.registerLevel(level)

            return level
        }

        private fun getGenerator(mod: KhsMod, id: Identifier, type: World.Type): ChunkGenerator {
            val server = mod.server.inner
            val defaultGen = server.overworld().chunkSource.generator

            if (isMapSave(id.path)) {
                return voidGenerator(mod)
            }

            return when (type) {
                World.Type.NETHER -> server.getLevel(Level.NETHER)?.chunkSource?.generator
                World.Type.END -> server.getLevel(Level.END)?.chunkSource?.generator
                World.Type.FLAT -> flatGenerator(mod)
                else -> null
            } ?: defaultGen
        }

        private fun getDimension(type: World.Type): ResourceKey<DimensionType> {
            when (type) {
                World.Type.NETHER -> return BuiltinDimensionTypes.NETHER
                World.Type.END -> return BuiltinDimensionTypes.END
                else -> return BuiltinDimensionTypes.OVERWORLD
            }
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
    }
}
