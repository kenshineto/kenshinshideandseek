package cat.freya.khs

import cat.freya.khs.config.EffectConfig
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.Board
import cat.freya.khs.player.Inventory
import cat.freya.khs.player.Player
import cat.freya.khs.world.Effect
import cat.freya.khs.world.Item
import cat.freya.khs.world.Material
import cat.freya.khs.world.World
import java.nio.file.Path
import java.util.UUID

// Plugin wrapper
interface KhsShim {
    /// String of the plugin version
    val pluginVersion: String

    /// String of the minecraft server version
    val mcVersionString: String

    /// Platform name that this shim is for
    val platform: String

    // Logger wrapper
    // (different baselines may use different logging systems)
    interface Logger {
        fun info(message: String)

        fun warning(message: String)

        fun error(message: String)
    }

    /// The logger
    val logger: Logger

    /// List of online players
    val players: List<Player>

    /// List of world names
    val worlds: List<String>

    /// List of supported block material names
    val blocks: List<String>

    /// Directory where config files and sqlitedb are stored
    val dataDirectory: Path

    /// Returns a valid material description for a given unparsed material name
    /// such as "CRAFTBENCH" or "white_wool"
    fun parseMaterial(materialName: String): Material?

    /// Returns a platform item implementation provided a item configuration/specification
    fun parseItem(itemConfig: ItemConfig): Item?

    /// Returns a platform potion effect implementation provided a item configuration/specification
    fun parseEffect(effectConfig: EffectConfig): Effect?

    /// Returns an online player based on the players UUID
    fun getPlayer(uuid: UUID): Player?

    /// Returns an online player based on the players display name
    fun getPlayer(name: String): Player?

    /// Wraps a retrieved platform player type into the wrapped player type.
    /// Packet events likes to give us a "Object" that is a BukkitPlayer (bukkit) or
    /// MinecraftServerPlayer (fabric)
    fun wrapPlayer(inner: Any?): Player?

    /// Returns an existing and loaded world on the server
    fun getWorld(worldName: String): World?

    /// Returns a manager to load/unload a world
    fun getWorldLoader(worldName: String): World.Loader

    /// Create a new world
    fun createWorld(worldName: String, type: World.Type): World?

    /// Create an inventory to use for a player
    fun createInventory(title: String, size: UInt): Inventory?

    /// Returns a new scoreboard to be displayed on the players
    /// right hand side of the screen
    fun getBoard(name: String): Board?

    /// Broadcast a message to everyone on the server
    fun broadcast(message: String)

    /// Kill the plugin now
    fun disable()

    /// Schedule an event to run at a later date
    fun scheduleEvent(ticks: ULong, event: () -> Unit)

    /// If the platform supports a given mc version features
    fun supports(vararg versions: Int): Boolean
}

abstract class AbstractKhsShim(override val platform: String) : KhsShim {

    /// Release minecraft version (ignores the 1.)
    val mcVersion: List<UInt> = parseMcVersion(mcVersionString)

    /// helper function that turns "26.1" into listOf(26u, 1u)
    private fun parseMcVersion(version: String?): List<UInt> {
        if (version == null) return emptyList()
        return version
            .split('.')
            .asSequence()
            .mapNotNull { it.toUIntOrNull() }
            // the 1. in old 1.x.x releases is useless
            .let { seq -> if (seq.firstOrNull() == 1u) seq.drop(1) else seq }
            .toList()
    }

    override fun supports(vararg versions: Int): Boolean {
        val seq = versions.asSequence().map { it.toUInt() }.zip(mcVersion.asSequence()).toList()
        for ((want, has) in seq) {
            if (want < has) return true
            if (want > has) return false
        }
        return true
    }
}
