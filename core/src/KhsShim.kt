package cat.freya.khs

import cat.freya.khs.config.EffectConfig
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.Board
import cat.freya.khs.menu.Inventory
import cat.freya.khs.type.Effect
import cat.freya.khs.type.Item
import cat.freya.khs.type.Material
import cat.freya.khs.world.Player
import cat.freya.khs.world.World
import java.nio.file.Path
import java.util.UUID

/** Root interface that allows the core mod/plugin interface with the underlying platform */
interface KhsShim {
    /** Friendly string of the mod/plugin version */
    val pluginVersion: String

    /** Friendly string of the minecraft server version */
    val serverVersion: String

    /** Name of the platform we are running on (e.g. Bukkit) */
    val platform: String

    interface Logger {
        fun info(message: String)

        fun warning(message: String)

        fun error(message: String)
    }

    /** Platforms implementation of a logger */
    val logger: Logger

    /** Directory where config files and sqlitedb are stored */
    val dataDirectory: Path

    /** @return a list of valid known materials */
    fun getMaterials(): List<Material>

    /** @return a list of valid block materials */
    fun getBlocks(): List<Material>

    /**
     * Get a [Material] by its platform name
     *
     * @return the material name for both the platform and current minecraft version
     */
    fun parseMaterial(platformKey: String): Material?

    /**
     * Parse an [Item] given its specification
     *
     * @return the parsed item or null
     */
    fun parseItem(itemConfig: ItemConfig): Item?

    /**
     * Parse an [Effect] given its specification
     *
     * @return the parsed effect or null
     */
    fun parseEffect(effectConfig: EffectConfig): Effect?

    /** @return list of currently online players */
    fun getPlayers(): List<Player>

    /** @return an online player by its uuid */
    fun getPlayer(uuid: UUID): Player?

    /** @return an online player by its username */
    fun getPlayer(name: String): Player?

    /**
     * Wraps a retrieved platform player type into the wrapped player type. Packet events likes to
     * give us an "Object" that is a BukkitPlayer (bukkit) or MinecraftServerPlayer (fabric)
     */
    fun wrapPlayer(inner: Any?): Player?

    /**
     * Send a player to a different server (requires bungeecord)
     *
     * @return if successful
     */
    fun sendPlayerToServer(uuid: UUID, server: String): Boolean

    /** @return a list of known world names */
    fun getWorldNames(): List<String>

    /** @return a loaded world on the server */
    fun getWorld(worldName: String): World?

    /** @return a manager to load/unload a world */
    fun getWorldLoader(worldName: String): World.Loader

    /**
     * Create a new world if it doesn't exist, or load a world if it does.
     *
     * @return a newly created world or null on failure
     */
    fun createWorld(worldName: String, type: World.Type): World?

    /** @return an empty custom inventory */
    fun createInventory(title: String, size: UInt): Inventory?

    /** @return a manager to a custom scoreboard */
    fun getBoard(name: String): Board?

    /** Broadcast a message to everyone on the server */
    fun broadcast(message: String)

    /** Kills the mod/plugin now! */
    fun disable()

    /** Schedules an event to take place in the future */
    fun scheduleEvent(ticks: ULong, event: () -> Unit)

    /** Checks if the minecraft server is greater or equal to the given version */
    fun supports(vararg versions: Int): Boolean
}

abstract class AbstractKhsShim(override val platform: String) : KhsShim {
    /** Parsed minecraft server version string */
    private var parsedServerVersion: List<UInt>? = null

    /** Helper function that turns "26.1" into listOf(26u, 1u) */
    private fun parseServerVersion(version: String?): List<UInt> {
        if (version == null) return emptyList()
        return version
            .split('.')
            .asSequence()
            .mapNotNull { it.toUIntOrNull() }
            // the 1. in old 1.x.x releases is useless
            .let { seq -> if (seq.firstOrNull() == 1u) seq.drop(1) else seq }
            .toList()
    }

    override fun getBlocks(): List<Material> {
        return getMaterials().filter { it.isBlock }
    }

    // don't make this vararg over UInt, otherwise kotlin complains
    // about "unstable features"
    override fun supports(vararg versions: Int): Boolean {
        if (parsedServerVersion == null) {
            parsedServerVersion = parseServerVersion(serverVersion)
        }

        val parsed = parsedServerVersion ?: return false
        val count = minOf(versions.size, parsed.size)

        for (i in 0 until count) {
            val want = versions[i].toUInt()
            val has = parsed[i]

            if (want < has) return true
            if (want > has) return false
        }

        return true
    }
}
