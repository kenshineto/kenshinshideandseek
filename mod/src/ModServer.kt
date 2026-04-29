package cat.freya.khs.mod

import cat.freya.khs.mod.KhsMod
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.TickEvent
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.LevelResource
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import java.nio.file.Path
import java.util.UUID

class ModServer(val mod: KhsMod) {
    private var server: MinecraftServer? = null
    private val tasks: MutableSet<() -> Boolean> = mutableSetOf()

    private val activeScoreBoards: MutableMap<UUID, String> = mutableMapOf()

    // allow non-null access to MinecraftServer, but also add sanity
    // checks
    val inner: MinecraftServer
        get() = server ?: error("inner called before initialization")

    // called when our mod is being initialized
    fun init() {
        // register core event listeners
        // we cannot initialize yet since we don't
        // have access to a MinecraftServer instance yet
        TickEvent.SERVER_POST.register { _ ->
            handleScheduledTasks()
            mod.onTick()
        }

        LifecycleEvent.SERVER_STOPPING.register { _ ->
            mod.onShutdown()
        }

        LifecycleEvent.SERVER_BEFORE_START.register { server ->
            this.server = server
            mod.init()
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

    fun getPlayer(uuid: UUID): ModPlayer? {
        return inner.playerList.getPlayer(uuid)?.let { ModPlayer(mod, it) }
    }

    fun getPlayer(name: String): ModPlayer? {
        return inner.playerList.getPlayer(name)?.let { ModPlayer(mod, it) }
    }

    fun getPlayers(): List<ModPlayer> {
        return inner.playerList.players.map { ModPlayer(mod, it) }
    }

    fun getWorld(name: String): ModWorld? {
        val id = Identifier.tryParse(name) ?: return null
        val key = ResourceKey.create(Registries.DIMENSION, id)
        return getWorld(key)
    }

    fun getWorld(key: ResourceKey<Level>): ModWorld? {
        return inner.getLevel(key)?.let { ModWorld(mod, it) }
    }

    fun getWorlds(): List<ModWorld> {
        return inner.allLevels.map { ModWorld(mod, it) }
    }

    fun getWorldContainer(): Path {
        return inner.getWorldPath(LevelResource("dimensions"))
    }

    private fun getDefaultObjective(): Objective? {
        val scoreboard = inner.scoreboard
        return scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR)
    }

    fun getScoreBoard(uuid: UUID): ModBoard {
        val scoreboard = inner.scoreboard
        val current = activeScoreBoards[uuid]
        if (current != null) {
            return getScoreBoard(current)
        }

        return ModBoard(scoreboard, getDefaultObjective())
    }

    fun getScoreBoard(name: String): ModBoard {
        val scoreboard = inner.scoreboard
        val objective =
            scoreboard.getObjective(name)
                ?: scoreboard.addObjective(
                    name,
                    ObjectiveCriteria.DUMMY,
                    KhsMod.parseText(name),
                    ObjectiveCriteria.RenderType.INTEGER,
                    true,
                    null,
                )

        return ModBoard(scoreboard, objective)
    }

    fun setScoreBoard(player: ModPlayer, board: ModBoard): Objective? {
        val objective = board.objective ?: getDefaultObjective()

        if (objective == null) {
            activeScoreBoards.remove(player.uuid)
        } else {
            activeScoreBoards[player.uuid] = objective.name
        }

        return objective
    }
}
