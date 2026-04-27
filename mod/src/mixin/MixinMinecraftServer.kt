package cat.freya.khs.mod.mixin

import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(MinecraftServer::class)
interface MixinMinecraftServer {
    @Accessor("levels")
    fun getLevels(): MutableMap<ResourceKey<Level>, ServerLevel>
}
