package cat.freya.khs.mod.mixin

import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.LevelStorageSource
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(MinecraftServer::class)
interface MinecraftServerAccess {
    @Accessor fun getLevels(): MutableMap<ResourceKey<Level>, ServerLevel>

    @Accessor fun getStorageSource(): LevelStorageSource.LevelStorageAccess
}
