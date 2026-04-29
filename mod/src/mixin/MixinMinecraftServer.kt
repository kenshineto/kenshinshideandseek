package cat.freya.khs.mod.mixin

import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.players.PlayerList
import net.minecraft.world.level.Level
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import java.util.concurrent.Executor

interface MixinInitLevel {
    fun initLevel(level: ServerLevel)

    fun removeLevel(key: ResourceKey<Level>, save: Boolean)
}

@Mixin(MinecraftServer::class)
abstract class MixinMinecraftServer : MixinInitLevel {
    @Shadow
    lateinit var levels: MutableMap<ResourceKey<Level>, ServerLevel>

    @Shadow
    lateinit var executor: Executor

    @Shadow
    abstract fun getAbsoluteMaxWorldSize(): Int

    @Shadow
    abstract fun getPlayerList(): PlayerList

    override fun initLevel(level: ServerLevel) {
        val key = level.dimension()

        if (levels.containsKey(key)) {
            // level already loaded
            return
        }

        executor.execute {
            levels.put(key, level)
            level.getWorldBorder().setAbsoluteMaxSize(getAbsoluteMaxWorldSize())
            getPlayerList().addWorldborderListener(level)
        }
    }

    override fun removeLevel(key: ResourceKey<Level>, save: Boolean) {
        val level = levels.remove(key) ?: return

        if (save) {
            level.save(null, true, true)
        }

        level.chunkSource.close()
    }
}
