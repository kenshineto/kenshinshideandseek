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
import java.util.concurrent.ConcurrentHashMap

/// Plugin wrapper
class Khs(val shim: KhsShim) {

    @Volatile var config: KhsConfig = KhsConfig()
    @Volatile var itemsConfig: KhsItemsConfig = KhsItemsConfig()
    @Volatile var boardConfig: KhsBoardConfig = KhsBoardConfig()
    @Volatile var locale: KhsLocale = KhsLocale()

    // code should access maps.<name>.config instead
    private var mapsConfig: KhsMapsConfig = KhsMapsConfig()

    val game: Game = Game(this)
    val maps: MutableMap<String, KhsMap> = ConcurrentHashMap<String, KhsMap>()
    @Volatile var database: Database? = null

    val commandGroup: CommandGroup = registerCommands()

    // block hunt
    val disguiser: Disguiser = Disguiser()
    val entityHider: EntityHider = EntityHider()

    // if we are performing a map save right now
    @Volatile var saving: Boolean = false

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

        val mcVersion = shim.mcVersion.joinToString(".")
        val fullMcVersion = "${ansiGray}Running on $mcVersion-${shim.platform}"
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

    fun reloadConfig(): Result<Unit> =
        runCatching {
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
            .onFailure { shim.logger.error("failed to reload config: ${it.message}") }

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
