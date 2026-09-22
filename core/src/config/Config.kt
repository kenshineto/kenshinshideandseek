package cat.freya.khs.config

import cat.freya.khs.KhsTypes
import cat.freya.khs.world.Location
import kotlin.UInt
import kotlin.annotation.AnnotationTarget
import kotlin.math.max

@Target(AnnotationTarget.PROPERTY) annotation class Section(val text: String)

@Repeatable @Target(AnnotationTarget.PROPERTY) annotation class Comment(val text: String)

@Target(AnnotationTarget.PROPERTY) annotation class Omittable

@Target(AnnotationTarget.PROPERTY) annotation class KhsDeprecated(val since: String)

enum class ConfigCountdownDisplay {
    CHAT,
    ACTIONBAR,
    TITLE,
}

enum class ConfigGameMode {
    HIDE_AND_SEEK,
    TAG,
}

enum class ConfigScoringMode(val minHiders: UInt) {
    ALL_HIDERS_FOUND(1u),
    LAST_HIDER_WINS(2u),
}

enum class ConfigLeaveType {
    EXIT,
    PROXY,
}

data class DelayedRespawnConfig(
    var enabled: Boolean = true,
    @Comment("How long do players have to wait in seconds before respawning") var delay: UInt = 5u,
)

enum class DatabaseType {
    SQLITE,
    MEMORY,
    MYSQL,
    POSTGRES,
    DISABLED,
}

data class DatabaseConfig(
    @Comment("The type of database to store user data in")
    @Comment("SQLITE - local file in plugin directory, fine for most small servers")
    @Comment("MYSQL - remote sql server running mysql")
    @Comment("POSTGRES - remote sql server running postgresql")
    var type: DatabaseType = DatabaseType.SQLITE,
    @Comment("The following options are only required for mysql or postgres") var host: String = "localhost",
    var port: ULong? = null,
    var username: String = "postgres",
    var password: String = "postgres",
    var database: String = "postgres",
)

data class ItemConfig(
    @Omittable var name: String? = null,
    var material: String = "BLOCK",
    var lore: List<String> = emptyList(),
    var enchantments: Map<String, UInt> = emptyMap(),
    @Omittable var unbreakable: Boolean? = null,
    @Omittable var modelData: UInt? = null,
    @Omittable var owner: String? = null,
    @Omittable var effect: String? = null,
    var slot: UInt? = null,
) {
    fun migrate(types: KhsTypes) {
        val parts = material.uppercase().split(":")
        if (parts.size != 2) return

        // migrate effect field
        if (types.isPotion(parts[0]) && effect == null) {
            material = parts[0]
            effect = parts[1]
        }
    }
}

data class EffectConfig(
    var type: String = "NONE",
    var duration: UInt = 60u,
    var amplifier: UInt = 1u,
    var ambient: Boolean = true,
    var particles: Boolean = true,
)

data class TauntConfig(
    var enabled: Boolean = true,
    @Comment("The delay in seconds between taunts, minimum is 60 seconds") var delay: ULong = 360u,
    @Comment("If enabled, taunts will be disabled if there is only a single hider left")
    var disableForLastHider: Boolean = false,
    @Comment("Allow seekers to see the time till next taunt, not just hiders") var showCountdown: Boolean = true,
) {
    fun migrate() {
        delay = max(delay, 60u)
    }
}

data class GlowConfig(
    var enabled: Boolean = true,
    @Comment("The length in seconds that the power-up lasts") var time: ULong = 30u,
    @Comment("Allows multiple uses of the power-up to stack the duration") var stackable: Boolean = true,
    @Comment("The config for the power-up item") var item: ItemConfig,
) {
    companion object {
        fun default(types: KhsTypes) =
            GlowConfig(
                item =
                    ItemConfig(
                        name = "Glow Power-up",
                        material = types.snowball,
                        lore =
                            listOf(
                                "Throw to make all seekers glow",
                                "Last 30s, all hiders can see it",
                                "Time stacks on multi use",
                            ),
                    )
            )
    }
}

data class LobbyConfig(
    @Comment("Time in seconds the lobby waits until the game starts. Set to 0 to disable") var countdown: ULong = 60u,
    @Comment("Player threshold to speed up the countdown to 10 seconds. Set to 0 to disable")
    var changeCountdown: UInt = 5u,
    @Comment("Minimum amount of players required to start the countdown") var min: UInt = 3u,
    @Comment("Maximum amount of players allowed in a lobby") var max: UInt = 10u,
    @Comment("Item to leave the lobby") var leaveItem: ItemConfig,
    @Comment("Admin item to force start the game") var startItem: ItemConfig,
) {
    companion object {
        fun default(types: KhsTypes) =
            LobbyConfig(
                leaveItem =
                    ItemConfig(
                        name = "&c Leave Lobby",
                        material = types.bed,
                        lore = listOf("Go back to server hub"),
                        slot = 0u,
                    ),
                startItem = ItemConfig(name = "&bStart Game", material = types.clock, slot = 8u),
            )
    }
}

data class SpectatorItemsConfig(
    /** Item for spectators to toggle flight */
    var flight: ItemConfig,
    /** Item for spectators to teleport to other players */
    var teleport: ItemConfig,
) {
    companion object {
        fun default(types: KhsTypes) =
            SpectatorItemsConfig(
                flight =
                    ItemConfig(
                        name = "&bToggle Flight",
                        material = types.feather,
                        lore = listOf("Turns flying on and off"),
                        slot = 6u,
                    ),
                teleport =
                    ItemConfig(
                        name = "&bTeleport to Others",
                        material = types.compass,
                        lore = listOf("Allows you to teleport to all other players in game"),
                        slot = 3u,
                    ),
            )
    }
}

data class SeekerPingDistancesConfig(
    var level1: UInt = 30u,
    var level2: UInt = 20u,
    var level3: UInt = 10u,
)

data class SeekerPingConfigSounds(
    @Comment("The noise for the heartbeat") var heartbeatNoise: String,
    @Comment("The noise for the ringing") var ringingNoise: String,
    var leadingVolume: Double = 0.5,
    var volume: Double = 0.3,
    var pitch: Double = 1.0,
) {
    companion object {
        fun default(types: KhsTypes) =
            SeekerPingConfigSounds(
                heartbeatNoise = types.noteBlockBaseDrum,
                ringingNoise = types.noteBlockPling,
            )
    }
}

data class SeekerPingConfig(
    var enabled: Boolean = true,
    @Comment("The distances for the volume to change")
    var distances: SeekerPingDistancesConfig = SeekerPingDistancesConfig(),
    @Comment("The sounds that players will hear") var sounds: SeekerPingConfigSounds,
) {
    companion object {
        fun default(types: KhsTypes) = SeekerPingConfig(sounds = SeekerPingConfigSounds.default(types))
    }
}

data class CommandHooksConfig(
    @Comment("When enabled, the plugin will execute the commands for each player when the game starts and when it ends")
    var enable: Boolean = false,
    @Comment(
        "Commands to execute when the game starts for every player. Use {name} for player name and {uuid} for their UUID"
    )
    var onGameStart: List<String> = listOf(""),
    @Comment(
        "Commands to execute when the game ends for every player on the winning team. Use {name} for player name and {uuid} for their UUID"
    )
    var onGameEndWin: List<String> = listOf(""),
    @Comment(
        "Commands to execute when the game ends for every player on the losing team. Use {name} for player name and {uuid} for their UUID"
    )
    var onGameEndLose: List<String> = listOf(""),
)

data class KhsConfig(
    // General
    @Section("General")
    @Comment("Notify plugin admins of new updates (requires hs.debug permission)")
    var checkForUpdates: Boolean = true,
    @Comment("Allow players to drop their items mid-game") var dropItems: Boolean = false,
    @Comment("Where the plugin will state the length of time in seconds left to hide.")
    @Comment("Below you can set CHAT, ACTIONBAR, or TITLE. Any invalid option will revert to CHAT.")
    var countdownDisplay: ConfigCountdownDisplay = ConfigCountdownDisplay.CHAT,
    @Comment("Allow Hiders to see everyone's nametags. Seeker can never see nametags.")
    var nametagsVisible: Boolean = false,
    @Comment("Require players to have permissions to run commands") var permissionsRequired: Boolean = true,
    @Comment("Amount of initial seekers when the game starts, minimum of 1") var startingSeekerCount: UInt = 1u,
    @Comment("If enabled, a HIDER will join the SPECTATOR team on death instead of the SEEKER team.")
    var respawnAsSpectator: Boolean = false,
    @Comment("Along with a chat message, display a title describing the game over") var gameOverTitle: Boolean = true,
    @Comment("Configure items given to spectators") var spectatorItems: SpectatorItemsConfig,
    @Comment("Configure the sounds that plays when a seeker is near") var seekerPing: SeekerPingConfig,
    @Comment("If to notify a seeker if they revealed a player in block hunt") var blockHuntNotify: Boolean = true,
    @Comment("For developers") var debug: Boolean = false,
    // Timing
    @Section("Timing")
    @Comment("How long in seconds will the game last, set to 0 to make game length infinite")
    var gameLength: ULong = 1200u,
    @Comment("How long in seconds will the initial hiding period last, minimum is 10 seconds")
    var hidingLength: ULong = 30u,
    @Comment("The amount of seconds the game will wait until the players are teleported to the lobby after a game over")
    var endGameDelay: ULong = 5u,
    @Comment("When enabled, seekers will have to wait [delay] seconds until they respawn in after death.")
    var delayedRespawn: DelayedRespawnConfig = DelayedRespawnConfig(),
    // Database
    @Section("Database") var database: DatabaseConfig = DatabaseConfig(),
    // Game Mode
    @Section("Game Mode")
    @Comment("Choose the game mode that the plugin will operate under.")
    @Comment("HIDE_AND_SEEK - Hiders become Seekers when found. See `scoringMode` for win condition.")
    @Comment("TAG - Seeker swaps with a Hider when they tag someone. Hiders win once the timer runs out.")
    var gameMode: ConfigGameMode = ConfigGameMode.HIDE_AND_SEEK,
    @Comment("The scoring mode decides the hider win condition for the HIDE_AND_SEEK game mode.")
    @Comment("ALL_HIDERS_FOUND - Any hiders left win once the timer runs out wins.")
    @Comment("LAST_HIDER_WINS - The last hider left wins immediately, or the remaining hiders win when")
    @Comment("                  the timer runs out.")
    var scoringMode: ConfigScoringMode = ConfigScoringMode.ALL_HIDERS_FOUND,
    @Comment("Trigger a win type of NONE if a player leaving the game triggers a win condition.")
    @Comment("This can be used as a way to prevent players from quitting to get someone else points.")
    var dontRewardQuit: Boolean = true,
    // PVP
    @Section("PVP")
    @Comment("If enabled, a seeker must sucessfully kill a hider in pvp to 'find' that hider.")
    @Comment("If disabled, a single tap by a seeker will mark the hider as found. ")
    @Comment("Items for pvp may be configured in the items.yml file")
    var pvp: Boolean = true,
    @Comment("Allow players to regen health") var regenHealth: Boolean = false,
    @Comment("If pvp is disabled, Hiders and Seekers can no longer take damage from natural causes")
    @Comment("unless this option is enabled. Such natural causes could be fall damage or projectiles.")
    var allowNaturalCauses: Boolean = false,
    // Lobby
    @Section("Lobby")
    @Comment("Players that join the server will automatically be added into a game lobby")
    var autoJoin: Boolean = false,
    @Comment("When players join the world containing the lobby, teleport them to")
    @Comment("the designated exit position so that they dont spawn in the loby while")
    @Comment("not in the queue. This setting is ignored when autoJoin is set to true.")
    var teleportStraysToExit: Boolean = false,
    @Comment("How to handle players leaving a game lobby.")
    @Comment("EXIT - Teleport the player to the designated exit location")
    @Comment("PROXY - Teleport the player to another server in a bungeecord/velocity network")
    var leaveType: ConfigLeaveType = ConfigLeaveType.EXIT,
    @Comment("The server to teleport to when leaveType is set to PROXY") var leaveServer: String = "lobby",
    @Comment("If to leave the game lobby after a game ends") var leaveOnEnd: Boolean = false,
    @Comment("Configure the \"waiting for players\" per map lobby") var lobby: LobbyConfig,
    @Comment("Restore the players previously cleared inventory after leaving the game lobby")
    var saveInventory: Boolean = false,
    @Comment("Restore the players previously active score board after leaving the game lobby")
    var saveScoreBoard: Boolean = true,
    // Events
    @Section("Events") @Comment("Taunt event") var taunt: TauntConfig = TauntConfig(),
    // Power-ups
    @Section("Power-ups") @Comment("Glow power-up") var glow: GlowConfig,
    @Comment("Instead of having a glow power-up, always make seekers' position's known to hiders at all times.")
    var alwaysGlow: Boolean = false,
    // Protections
    @Section("Protections")
    @Comment("When enabled, the plugin will duplicate the hide and seek map to protect the")
    @Comment("original from changes during a game. It is highly recommended that you keep this")
    @Comment("to true unless you have other means of protecting your hide-and-seek map.")
    var mapSaveEnabled: Boolean = true,
    @Comment("Block these commands for players in a game. Good for blocking communication")
    var blockedCommands: List<String> = listOf("msg", "reply", "me", "kill"),
    @Comment("Don't allow players to interact with these blocks") var blockedInteracts: List<String>,
    @Section("Command Hooks")
    @Comment("Trigger custom commands after game events")
    var commandHooks: CommandHooksConfig = CommandHooksConfig(),
    // Auto Generated
    @Section("Auto Generated")
    @Comment("Location where players are teleported to when they run (/hs leave).")
    var exit: Location? = null,
) {
    fun migrate(types: KhsTypes) {
        // migrate items
        glow.item.migrate(types)
        lobby.leaveItem.migrate(types)
        lobby.startItem.migrate(types)
        spectatorItems.flight.migrate(types)
        spectatorItems.teleport.migrate(types)

        // migrate minimum values
        startingSeekerCount = max(startingSeekerCount, 1u)
        hidingLength = max(hidingLength, 10u)
    }

    companion object {
        fun default(types: KhsTypes) =
            KhsConfig(
                glow = GlowConfig.default(types),
                lobby = LobbyConfig.default(types),
                seekerPing = SeekerPingConfig.default(types),
                spectatorItems = SpectatorItemsConfig.default(types),
                blockedInteracts = listOf(types.furnace, types.craftingTable, types.anvil, types.chest, types.barrel),
            )
    }
}
