package cat.freya.khs.fabric

import java.nio.file.Path
import java.util.UUID
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.LevelResource
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.criteria.ObjectiveCriteria

class FabricServer(val mod: KhsMod) {

    private var server: MinecraftServer? = null
    private val tasks: MutableSet<() -> Boolean> = mutableSetOf()

    private val activeScoreBoards: MutableMap<UUID, String> = mutableMapOf()

    // allow non null access to MinecraftServer, but also add sanity
    // checks
    val inner: MinecraftServer
        get() = server ?: error("inner called before initalization")

    // called when our mod is being initalized
    fun init() {
        // register core event listeners
        // we cannot initalize yet since we dont
        // have access to a MinecraftServer instance yet
        ServerTickEvents.END_SERVER_TICK.register { server ->
            // see if we need to initalize
            if (this.server == null) {
                this.server = server
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

    fun getPlayer(uuid: UUID): FabricPlayer? {
        return inner.playerList.getPlayer(uuid)?.let { FabricPlayer(mod, it) }
    }

    fun getPlayer(name: String): FabricPlayer? {
        return inner.playerList.getPlayer(name)?.let { FabricPlayer(mod, it) }
    }

    fun getPlayers(): List<FabricPlayer> {
        return inner.playerList.players.map { FabricPlayer(mod, it) }
    }

    fun getWorld(name: String): FabricWorld? {
        val id = Identifier.tryParse(name) ?: return null
        val key = ResourceKey.create(Registries.DIMENSION, id)
        return getWorld(key)
    }

    fun getWorld(key: ResourceKey<Level>): FabricWorld? {
        return inner.getLevel(key)?.let { FabricWorld(mod, it) }
    }

    fun getWorlds(): List<FabricWorld> {
        return inner.getAllLevels().map { FabricWorld(mod, it) }
    }

    fun getWorldContainer(): Path {
        return inner.getWorldPath(LevelResource.ROOT)
    }

    private fun getDefaultObjective(): Objective? {
        val scoreboard = inner.scoreboard
        return scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR)
    }

    fun getScoreBoard(uuid: UUID): FabricBoard {
        val scoreboard = inner.scoreboard
        val current = activeScoreBoards[uuid]
        if (current != null) {
            return getScoreBoard(current)
        }

        return FabricBoard(scoreboard, getDefaultObjective())
    }

    fun getScoreBoard(name: String): FabricBoard {
        val scoreboard = inner.scoreboard
        val objective =
            scoreboard.getObjective(name)
                ?: scoreboard.addObjective(
                    name,
                    ObjectiveCriteria.DUMMY,
                    Component.literal(name),
                    ObjectiveCriteria.RenderType.INTEGER,
                    true,
                    null,
                )

        return FabricBoard(scoreboard, objective)
    }

    fun setScoreBoard(player: FabricPlayer, board: FabricBoard): Objective? {
        val objective = board.objective ?: getDefaultObjective()

        if (objective == null) {
            activeScoreBoards.remove(player.uuid)
        } else {
            activeScoreBoards.put(player.uuid, objective.name)
        }

        return objective
    }
}
