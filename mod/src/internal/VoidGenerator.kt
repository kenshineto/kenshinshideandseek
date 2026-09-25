package cat.freya.khs.mod.internal

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.concurrent.CompletableFuture
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.server.level.WorldGenRegion
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.level.NoiseColumn
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.BiomeSource
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.RandomState
import net.minecraft.world.level.levelgen.blending.Blender
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext

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
