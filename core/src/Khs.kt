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
import cat.freya.khs.game.Game
import cat.freya.khs.game.KhsMap
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

    // if we are performing a map save right now
    @Volatile var saving: Boolean = false

    fun init() {
        shim.logger.info(" _  ___   _ ____")
        shim.logger.info("| |/ / | | / ___|")
        shim.logger.info("| ' /| |_| \\___ \\")
        shim.logger.info("| . \\|  _  |___) |")
        shim.logger.info("|_|\\_\\_| |_|____/")

        val mcVersion = shim.mcVersion.joinToString(".")
        shim.logger.info("Version ${shim.pluginVersion} running on ${mcVersion}-${shim.platform}")

        reloadConfig()
            .onFailure {
                shim.logger.warning("Plugin loaded with errors :(")
                shim.disable()
            }
            .onSuccess {
                shim.logger.info("Plugin loaded successfully!")
                saveConfig()
            }
    }

    fun cleanup() {
        for (uuid in game.UUIDs) game.leave(uuid)
    }

    fun registerCommands(): CommandGroup {
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

    fun reloadConfig(): Result<Unit> =
        runCatching {
                shim.logger.info("Loading config...")
                config = deserialize(KhsConfig::class, shim.readConfigFile("config.yml"))
                shim.logger.info("Loading items...")
                itemsConfig = deserialize(KhsItemsConfig::class, shim.readConfigFile("items.yml"))
                shim.logger.info("Loading maps...")
                mapsConfig = deserialize(KhsMapsConfig::class, shim.readConfigFile("maps.yml"))
                shim.logger.info("Loading board locale...")
                boardConfig = deserialize(KhsBoardConfig::class, shim.readConfigFile("board.yml"))
                shim.logger.info("Loading locale...")
                locale = deserialize(KhsLocale::class, shim.readConfigFile("locale.yml"))
                shim.logger.info("Loading database...")
                database = Database(this)

                // reload maps
                // we need a seperate newMaps, in case one of the maps below fails
                // to load
                val newMaps =
                    mapsConfig.maps.mapValues { (name, mapConfig) -> KhsMap(name, mapConfig, this) }

                game.setMap(null)
                maps.clear()
                newMaps.forEach { maps[it.key] = it.value }
            }
            .onFailure { shim.logger.error("failed to reload config: ${it.message}") }

    fun saveConfig() {
        runCatching {
                val newMapsConfig = KhsMapsConfig(maps.mapValues { it.value.config })
                shim.writeConfigFile("config.yml", serialize(config))
                shim.writeConfigFile("items.yml", serialize(itemsConfig))
                shim.writeConfigFile("maps.yml", serialize(newMapsConfig))
                shim.writeConfigFile("board.yml", serialize(boardConfig))
                shim.writeConfigFile("locale.yml", serialize(locale))
            }
            .onFailure { shim.logger.error("failed to save config: ${it.message}") }
    }

    fun onTick() {
        game.doTick()
    }
}
