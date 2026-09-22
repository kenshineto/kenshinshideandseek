package cat.freya.khs.neoforge

import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlatform
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.dimension.DimensionType
import net.neoforged.fml.common.Mod

@Mod(KhsMod.ID)
class KhsModNeoForge : ModPlatform("NeoForge") {
    val mod = KhsMod(this)

    override fun createLevel(
        key: ResourceKey<Level>,
        dimension: ResourceKey<DimensionType>,
        generator: ChunkGenerator,
    ): ServerLevel? {
        // TODO:
        return null
    }

    override fun unloadLevel(key: ResourceKey<Level>) {
        // TODO:
    }
}
