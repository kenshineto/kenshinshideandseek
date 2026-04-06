package cat.freya.khs

import cat.freya.khs.command.*
import cat.freya.khs.command.map.*
import cat.freya.khs.command.map.blockhunt.*
import cat.freya.khs.command.map.blockhunt.block.*
import cat.freya.khs.command.map.set.*
import cat.freya.khs.command.map.unset.*
import cat.freya.khs.command.util.CommandGroup
import cat.freya.khs.command.world.*
import cat.freya.khs.config.KhsBoardConfig
import cat.freya.khs.config.KhsConfig
import cat.freya.khs.config.KhsItemsConfig
import cat.freya.khs.config.KhsLocale
import cat.freya.khs.config.KhsMapsConfig
import cat.freya.khs.config.util.deserialize
import cat.freya.khs.config.util.serialize
import cat.freya.khs.db.Database
import cat.freya.khs.disguise.Disguiser
import cat.freya.khs.disguise.EntityHider
import cat.freya.khs.game.Game
import cat.freya.khs.game.KhsMap
import cat.freya.khs.packet.KhsPacketListener
import java.io.File
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

data class Request(val fn: () -> Unit, val lengthSeconds: Long) {
    val start = System.currentTimeMillis()
    val expired: Boolean
        get() = (System.currentTimeMillis() - start) < lengthSeconds * 1000
}

/// Plugin wrapper
class Khs(val shim: KhsShim) {

    /** The main plugin config */
    var config: KhsConfig = KhsConfig()
        private set

    /** Stores seeker/hider items and effects */
    var itemsConfig: KhsItemsConfig = KhsItemsConfig()
        private set

    /** Stores format and strings for the game board */
    var boardConfig: KhsBoardConfig = KhsBoardConfig()
        private set

    /** Stores localized plugin messages */
    var locale: KhsLocale = KhsLocale()
        private set

    /**
     * Stores all maps known to the plugin. Map configurations should be accessed though
     * maps.<name>.config
     *
     * This config is auto generated and should not be touched directly. It is generated based on
     * the state of `maps`
     */
    private var mapsConfig: KhsMapsConfig = KhsMapsConfig()

    /**
     * The current single game for the plugin
     *
     * Should we store multiple games?
     */
    val game: Game = Game(this)

    /** Deserialize maps known to the plugin from `mapsConfig` */
    val maps: MutableMap<String, KhsMap> = ConcurrentHashMap<String, KhsMap>()

    /** Stores players name and win/loss/kill/death information */
    var database: Database? = null
        private set

    /** Commands known to this plugin */
    val commandGroup: CommandGroup = registerCommands()

    /** Holds current disguises for disgusied players */
    val disguiser: Disguiser = Disguiser()

    /** Allows hiding entities for only some player observers */
    val entityHider: EntityHider = EntityHider()

    /** Knowns requests that need to be completed with `/hs confirm` */
    val requests: MutableMap<UUID, Request> = ConcurrentHashMap<UUID, Request>()

    /** If a map save is currently in progress */
    val saving: AtomicBoolean = AtomicBoolean(false)

    fun init() {
        printBanner()
        reloadConfig()
            .onFailure {
                shim.logger.warning("Plugin loaded with errors :(")
                shim.disable()
            }
            .onSuccess {
                shim.logger.info("Plugin loaded successfully!")
                saveConfig()
            }

        KhsPacketListener(this)
    }

    fun cleanup() {
        for (uuid in game.playerUUIDs) game.leave(uuid)
        disguiser.cleanup()
    }

    private fun printBanner() {
        val ansiReset = "\u001B[0m"
        val ansiBlue = "\u001B[94m"
        val ansiGreen = "\u001B[92m"
        val ansiGray = "\u001B[90m"

        val fullMcVersion = "${ansiGray}Running on ${shim.serverVersion}-${shim.platform}"
        val fullPluginVersion = "${ansiGreen}Version ${shim.pluginVersion}"

        shim.logger.info("$ansiBlue _  ___   _ ____$ansiReset")
        shim.logger.info("$ansiBlue| |/ / | | / ___|    $fullPluginVersion$ansiReset")
        shim.logger.info("$ansiBlue| ' /| |_| \\___ \\    $fullMcVersion$ansiReset")
        shim.logger.info("$ansiBlue| . \\|  _  |___) |$ansiReset")
        shim.logger.info("$ansiBlue|_|\\_\\_| |_|____/$ansiReset")
    }

    private fun registerCommands(): CommandGroup {
        return CommandGroup(
            this,
            "hs",
            KhsConfirm(),
            KhsDebug(),
            KhsHelp(),
            KhsJoin(),
            KhsLeave(),
            KhsReload(),
            KhsSend(),
            KhsSetExit(),
            KhsStart(),
            KhsStop(),
            KhsTop(),
            KhsWins(),
            CommandGroup(
                this,
                "map",
                KhsMapAdd(),
                KhsMapGoTo(),
                KhsMapList(),
                KhsMapRemove(),
                KhsMapSave(),
                KhsMapStatus(),
                CommandGroup(
                    this,
                    "blockhunt",
                    KhsMapBlockHuntDebug(),
                    KhsMapBlockHuntDisguise(),
                    KhsMapBlockHuntEnabled(),
                    CommandGroup(
                        this,
                        "block",
                        KhsMapBlockHuntBlockAdd(),
                        KhsMapBlockHuntBlockList(),
                        KhsMapBlockHuntBlockRemove(),
                    ),
                ),
                CommandGroup(
                    this,
                    "set",
                    KhsMapSetBorder(),
                    KhsMapSetBounds(),
                    KhsMapSetLobby(),
                    KhsMapSetSeekerLobby(),
                    KhsMapSetSpawn(),
                ),
                CommandGroup(this, "unset", KhsMapUnsetBorder()),
            ),
            CommandGroup(
                this,
                "world",
                KhsWorldCreate(),
                KhsWorldDelete(),
                KhsWorldList(),
                KhsWorldTp(),
            ),
        )
    }

    private fun readConfigFile(fileName: String): InputStream? {
        val dir = shim.dataDirectory.toFile()
        if (!dir.exists()) dir.mkdirs() || error("Failed to make plugin config directory")
        val file = File(dir, fileName)
        return if (file.exists()) file.inputStream() else null
    }

    fun reloadConfig(): Result<Unit> {
        return runCatching {
                shim.logger.info("Loading config...")
                config = deserialize(KhsConfig::class, readConfigFile("config.yml"))
                shim.logger.info("Loading items...")
                itemsConfig = deserialize(KhsItemsConfig::class, readConfigFile("items.yml"))
                shim.logger.info("Loading maps...")
                mapsConfig = deserialize(KhsMapsConfig::class, readConfigFile("maps.yml"))
                shim.logger.info("Loading board locale...")
                boardConfig = deserialize(KhsBoardConfig::class, readConfigFile("board.yml"))
                shim.logger.info("Loading locale...")
                locale = deserialize(KhsLocale::class, readConfigFile("locale.yml"))
                shim.logger.info("Loading database...")

                // database config could of changed so we need to
                // reconnect to the database
                database = Database(this)

                // reload maps
                // we need a separate newMaps, in case one of the maps below fails
                // to load
                val newMaps =
                    mapsConfig.maps.mapValues { (name, mapConfig) -> KhsMap(name, mapConfig, this) }

                game.setMap(null)
                maps.clear()
                newMaps.forEach { maps[it.key] = it.value }
            }
            .onFailure {
                shim.logger.error("failed to reload config: ${it.message}")
                for (line in it.stackTraceToString().lines()) shim.logger.error(line)
            }
    }

    private fun writeConfigFile(fileName: String, content: String) {
        val dir = shim.dataDirectory.toFile()
        if (!dir.exists()) dir.mkdirs() || error("Failed to make plugin config directory")
        val file = File(dir, fileName)
        file.writeText(content)
    }

    fun saveConfig() {
        runCatching {
                val newMapsConfig = KhsMapsConfig(maps.mapValues { it.value.config })
                writeConfigFile("config.yml", serialize(config))
                writeConfigFile("items.yml", serialize(itemsConfig))
                writeConfigFile("maps.yml", serialize(newMapsConfig))
                writeConfigFile("board.yml", serialize(boardConfig))
                writeConfigFile("locale.yml", serialize(locale))
            }
            .onFailure { shim.logger.error("failed to save config: ${it.message}") }
    }

    fun onTick() {
        game.doTick()
        disguiser.update()
    }
}
