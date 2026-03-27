package cat.freya.khs

import cat.freya.khs.config.EffectConfig
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.Board
import cat.freya.khs.player.Inventory
import cat.freya.khs.player.Player
import cat.freya.khs.world.Effect
import cat.freya.khs.world.Item
import cat.freya.khs.world.World
import java.io.InputStream
import java.util.UUID

// Logger wrapper
// (different baselines may use different logging systems)
interface Logger {
    fun info(message: String)

    fun warning(message: String)

    fun error(message: String)
}

// Plugin wrapper
interface KhsShim {
    /// @returns the string of the plugin version
    val pluginVersion: String

    /// @returns the release minecraft version (ignores the 1.)
    val mcVersion: List<UInt>

    /// the platform this shim is for
    val platform: String

    /// @returns the logger
    val logger: Logger

    /// @returns list of online players
    val players: List<Player>

    /// @returns list of world names
    val worlds: List<String>

    /// were the khs.db is stored
    val sqliteDatabasePath: String

    /// @returns a stream from a file in the systems config dir
    fun readConfigFile(fileName: String): InputStream?

    /// write a config file
    fun writeConfigFile(fileName: String, content: String)

    /// @returns a valid material for the current mc version given the name
    fun parseMaterial(materialName: String): String?

    /// @returns a valid item given the config
    fun parseItem(itemConfig: ItemConfig): Item?

    /// @returns a valid item given the config
    fun parseEffect(effectConfig: EffectConfig): Effect?

    /// @returns a player that is online on the server right now
    fun getPlayer(uuid: UUID): Player?

    fun getPlayer(name: String): Player?

    /// @returns a world on the server that exists with the given world name
    fun getWorld(worldName: String): World?

    /// @returns a manager to load/unload a world
    fun getWorldLoader(worldName: String): World.Loader

    /// create a new world
    fun createWorld(worldName: String, type: World.Type): World?

    /// create a inventory to use for a player
    fun createInventory(title: String, size: UInt): Inventory?

    /// @returns a new board
    fun getBoard(name: String): Board?

    /// broadcast a message to everyone
    fun broadcast(message: String)

    /// disable everything
    fun disable()

    /// schedule an event to run at a later date
    fun scheduleEvent(ticks: ULong, event: () -> Unit)

    fun supports(vararg versions: Int): Boolean {
        val seq = versions.asSequence().map { it.toUInt() }.zip(mcVersion.asSequence()).toList()
        for ((want, has) in seq) {
            if (want < has) return true
            if (want > has) return false
        }
        return true
    }
}
