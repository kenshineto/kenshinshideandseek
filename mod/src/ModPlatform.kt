package cat.freya.khs.mod

import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.dimension.DimensionType

abstract class ModPlatform(val name: String) {
    abstract fun createLevel(
        key: ResourceKey<Level>,
        dimension: ResourceKey<DimensionType>,
        generator: ChunkGenerator,
    ): ServerLevel?

    abstract fun unloadLevel(key: ResourceKey<Level>)
}
