package cat.freya.khs.mod

import cat.freya.khs.mod.internal.LevelManager
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.event.events.common.TickEvent
import java.nio.file.Path
import java.util.UUID
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.LevelResource
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.criteria.ObjectiveCriteria

class ModServer(val mod: KhsMod) {
    lateinit var inner: MinecraftServer

    private var levelManager: LevelManager? = null
    private val tasks: MutableSet<() -> Boolean> = mutableSetOf()

    private val activeScoreBoards: MutableMap<UUID, String> = mutableMapOf()
    private val playerSeenObjective: MutableMap<UUID, MutableSet<String>> = mutableMapOf()

    // called when our mod is being initialized
    fun init() {
        // register core event listeners
        // we cannot initialize yet since we don't
        // have access to a MinecraftServer instance yet
        TickEvent.SERVER_POST.register { _ ->
            handleScheduledTasks()
            mod.doTick()
        }

        LifecycleEvent.SERVER_STOPPING.register { _ ->
            mod.onShutdown()
        }

        LifecycleEvent.SERVER_BEFORE_START.register { server ->
            this.inner = server
            mod.init()
        }

        PlayerEvent.PLAYER_JOIN.register { player ->
            synchronized(playerSeenObjective) {
                playerSeenObjective.remove(player.uuid)
            }
        }
    }

    private fun handleScheduledTasks() {
        for (task in tasks) {
            val finished = task()
            if (finished) tasks.remove(task)
        }
    }

    fun scheduleTask(ticks: ULong, fn: () -> Unit) {
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

    fun levelManager(): LevelManager {
        val levelManager = this.levelManager ?: LevelManager(this)
        if (this.levelManager == null) {
            this.levelManager = levelManager
        }
        return levelManager
    }

    fun getWorld(name: String): ModWorld? {
        val id = Identifier.tryParse(name) ?: return null
        val key = ResourceKey.create(Registries.DIMENSION, id)
        return getWorld(key)
    }

    fun getWorld(key: ResourceKey<Level>): ModWorld? {
        val level = inner.getLevel(key) ?: levelManager().get(key) ?: return null
        return ModWorld(mod, level)
    }

    fun getWorlds(): List<ModWorld> {
        return inner.allLevels.map { ModWorld(mod, it) }
    }

    fun <T : Any> getRegistry(type: ResourceKey<out Registry<out T>>): Registry<T> {
        val registries = mod.server.inner.registryAccess()
        return registries.lookupOrThrow(type)
    }

    fun <T : Any> getMappedRegistry(type: ResourceKey<out Registry<out T>>): MappedRegistry<T> {
        return getRegistry(type) as MappedRegistry<T>
    }

    fun getWorldContainer(): Path {
        return inner.getWorldPath(LevelResource("dimensions"))
    }

    fun getPlayerScoreBoard(uuid: UUID): ModBoard {
        val current = activeScoreBoards[uuid]
        if (current != null) {
            return getScoreBoard(current)
        }

        val scoreboard = inner.scoreboard
        val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR)
        return ModBoard(mod, scoreboard, objective)
    }

    fun getScoreBoard(objectiveName: String): ModBoard {
        val scoreboard = inner.scoreboard
        val objective =
            scoreboard.getObjective(objectiveName)
                ?: scoreboard.addObjective(
                    objectiveName,
                    ObjectiveCriteria.DUMMY,
                    KhsMod.parseText(objectiveName),
                    ObjectiveCriteria.RenderType.INTEGER,
                    true,
                    null,
                )

        return ModBoard(mod, scoreboard, objective)
    }

    fun setScoreBoard(uuid: UUID, board: ModBoard) {
        if (board.objective == null) {
            activeScoreBoards.remove(uuid)
        } else {
            activeScoreBoards[uuid] = board.objective.name
        }
    }

    fun hasSeenObjective(uuid: UUID, objectiveName: String): Boolean {
        synchronized(playerSeenObjective) {
            val seen = playerSeenObjective[uuid] ?: mutableSetOf()
            if (seen.contains(objectiveName)) return true

            // mark as seen
            seen.add(objectiveName)
            playerSeenObjective[uuid] = seen

            return false
        }
    }

    fun dispatchCommand(command: String): Boolean =
        runCatching {
                val source = inner.createCommandSourceStack()
                source.dispatcher().execute(command, source)
                true
            }
            .getOrElse { false }
}
