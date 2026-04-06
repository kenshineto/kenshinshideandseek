package cat.freya.khs.fabric

import java.nio.file.Path
import java.util.UUID
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.LevelResource

class FabricKhsServer(val mod: KhsMod) {

    private var server: MinecraftServer? = null
    private var tasks: MutableSet<() -> Boolean> = mutableSetOf()

    // allow non null access to MinecraftServer, but also add sanity
    // checks
    val inner: MinecraftServer
        get() = server ?: error("inner called before initalization")

    // called when our mod is being initalized
    fun init(onInit: () -> Unit) {
        // register core event listeners
        // we cannot initalize yet since we dont
        // have access to a MinecraftServer instance yet
        ServerTickEvents.END_SERVER_TICK.register { server ->
            // see if we need to initalize
            if (this.server == null) {
                this.server = server
                onInit()
            }

            handleScheduledTasks()
            mod.onTick()
        }

        ServerLifecycleEvents.SERVER_STOPPING.register { _ ->
            mod.onShutdown()
            this.server = null
        }
    }

    private fun handleScheduledTasks() {
        for (task in tasks) {
            val finished = task()
            if (finished) tasks.remove(task)
        }
    }

    fun scheduleTask(fn: () -> Unit, ticks: ULong) {
        var ticksLeft = ticks
        tasks.add {
            if (ticksLeft == 0UL) {
                fn()
                return@add true
            }
            ticksLeft--
            return@add false
        }
    }

    fun getPlayer(uuid: UUID): FabricKhsPlayer? {
        return inner.playerList.getPlayer(uuid)?.let { FabricKhsPlayer(mod, it) }
    }

    fun getPlayer(name: String): FabricKhsPlayer? {
        return inner.playerList.getPlayer(name)?.let { FabricKhsPlayer(mod, it) }
    }

    fun getPlayers(): List<FabricKhsPlayer> {
        return inner.playerList.players.map { FabricKhsPlayer(mod, it) }
    }

    fun getWorld(name: String): FabricKhsWorld? {
        val id = Identifier.tryParse(name) ?: return null
        val key = ResourceKey.create(Registries.DIMENSION, id)
        return getWorld(key)
    }

    fun getWorld(key: ResourceKey<Level>): FabricKhsWorld? {
        return inner.getLevel(key)?.let { FabricKhsWorld(mod, it) }
    }

    fun getWorlds(): List<FabricKhsWorld> {
        return inner.getAllLevels().map { FabricKhsWorld(mod, it) }
    }

    fun getWorldContainer(): Path {
        return inner.getWorldPath(LevelResource.ROOT)
    }
}
