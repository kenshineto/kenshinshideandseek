package cat.freya.khs.config

import cat.freya.khs.world.Location
import kotlin.UInt
import kotlin.annotation.AnnotationTarget

@Target(AnnotationTarget.PROPERTY) annotation class Section(val text: String)

@Repeatable @Target(AnnotationTarget.PROPERTY) annotation class Comment(val text: String)

@Target(AnnotationTarget.PROPERTY) annotation class Omittable

@Target(AnnotationTarget.PROPERTY) annotation class KhsDeprecated(val since: String)

enum class ConfigCountdownDisplay {
    CHAT,
    ACTIONBAR,
    TITLE,
}

enum class ConfigScoringMode {
    ALL_HIDERS_FOUND,
    LAST_HIDER_WINS,
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
    MYSQL,
    POSTGRES,
}

data class DatabaseConfig(
    @Comment("The type of database to store user data in")
    @Comment("SQLITE - local file in plugin directory, fine for most small servers")
    @Comment("MYSQL - remote sql server running mysql")
    @Comment("POSTGRES - remote sql server running postgresql")
    var type: DatabaseType = DatabaseType.SQLITE,
    @Comment("The following options are only required for mysql or postgres")
    var host: String = "localhost",
    var port: ULong? = null,
    var username: String = "postgres",
    var password: String = "postgres",
    var database: String = "postgres",
)

data class ItemConfig(
    @Omittable var name: String? = null,
    var material: String = "DIRT",
    var lore: List<String> = emptyList(),
    var enchantments: Map<String, UInt> = emptyMap(),
    @Omittable var unbreakable: Boolean? = null,
    @Omittable var modelData: UInt? = null,
    @Omittable var owner: String? = null,
)

data class EffectConfig(
    var type: String = "SPEED",
    var duration: UInt = 60u,
    var amplifier: UInt = 1u,
    var ambient: Boolean = true,
    var particles: Boolean = true,
)

data class TauntConfig(
    var enabled: Boolean = true,
    @Comment("The delay in seconds between taunts, minimum is 60 seconds") var delay: ULong = 360u,
    @Comment("If to disable the taunt when there is only a single hider left")
    var disableForLastHider: Boolean = false,
    @Comment("Show the countdown till next taunt for everyone") var showCountdown: Boolean = true,
)

data class GlowConfig(
    var enabled: Boolean = true,
    @Comment("How long in seconds does the power up last") var time: ULong = 30u,
    @Comment("If multiple power-up uses can stack the time left") var stackable: Boolean = true,
    @Comment("The config for the power-up item")
    var item: ItemConfig =
        ItemConfig(
            name = "Glow Power-up",
            material = "SNOWBALL",
            lore =
                listOf(
                    "Throw to make all seekers glow",
                    "Last 30s, all hiders can see it",
                    "Time stacks on multi use",
                ),
        ),
)

data class LobbyConfig(
    @Comment("Time in seconds that the lobby waits until game starts. Set to 0 to disable")
    var countdown: ULong = 60u,
    @Comment("Player threshold to speed up the countdown. Set to 0 to disable")
    var changeCountdown: UInt = 5u,
    @Comment("Minimum amount of players required to start the countdown") var min: UInt = 3u,
    @Comment("Maximum amount of players allowed in a lobby") var max: UInt = 10u,
    @Comment("Item for players to use to leave the lobby")
    var leaveItem: ItemConfig =
        ItemConfig(
            name = "&c Leave Lobby",
            material = "BED",
            lore = listOf("Go back to server hub"),
        ),
    @Comment("Item for admins to use to force start the game")
    var startItem: ItemConfig = ItemConfig(name = "&bStart Game", material = "CLOCK"),
)

data class SpectatorItemsConfig(
    /// Item for spectators to toggle flight
    var flight: ItemConfig =
        ItemConfig(
            name = "&bToggle Flight",
            material = "FEATHER",
            lore = listOf("Turns flying on and off"),
        ),

    /// Item for spectators to teleport to other players
    var teleport: ItemConfig =
        ItemConfig(
            name = "&bTeleport to Others",
            material = "COMPASS",
            lore = listOf("Allows you to teleport to all other players in game"),
        ),
)

data class SeekerPingDistancesConfig(
    var level1: UInt = 30u,
    var level2: UInt = 20u,
    var level3: UInt = 10u,
)

data class SeekerPingConfigSounds(
    @Comment("The noise for the heartbeat")
    var heartbeatNoise: String = "BLOCK_NOTE_BLOCK_BASEDRUM",
    @Comment("The noise for the ringing") var ringingNoise: String = "BLOCK_NOTE_BLOCK_PLING",
    var leadingVolume: Double = 0.5,
    var volume: Double = 0.3,
    var pitch: Double = 1.0,
)

data class SeekerPingConfig(
    var enabled: Boolean = true,
    @Comment("The distances for the volume to change")
    var distances: SeekerPingDistancesConfig = SeekerPingDistancesConfig(),
    @Comment("The sounds that players will hear")
    var sounds: SeekerPingConfigSounds = SeekerPingConfigSounds(),
)

data class KhsConfig(
    /* General */

    @Section("General")
    @Comment("Allow players to drop their items in game")
    var dropItems: Boolean = false,
    @Comment("When the game is starting, the plugin will state there is x seconds left to hide.")
    @Comment(
        "You change where countdown messages are to be displayed: in the chat, action bar, or a title."
    )
    @Comment("Below you can set CHAT, ACTIONBAR, or TITLE. Any invalid option will revert to CHAT.")
    var countdownDisplay: ConfigCountdownDisplay = ConfigCountdownDisplay.CHAT,
    @Comment(
        "Allow Hiders to see their own teams nametags as well as seekers. Seekers can never see nametags regardless"
    )
    var nametagsVisible: Boolean = false,
    @Comment(
        "Require bukkit permissions though a permission plugin to run commands, or require op, recommended on most servers"
    )
    var permissionsRequired: Boolean = true,
    @Comment("Minimum amount of players to start the game. Cannot go lower than 2.")
    var minPlayers: UInt = 2u,
    @Comment("Amount of initial seekers when the game starts, minimum of 1")
    var startingSeekerCount: UInt = 1u,
    @Comment(
        "By default, when a HIDER dies they will join the SEEKER team. If enabled they will instead become a SPECTATOR."
    )
    var respawnAsSpectator: Boolean = false,
    @Comment("Along with a chat message, display a title describing the game over")
    var gameOverTitle: Boolean = true,
    @Comment("Configure items given to spectators")
    var spectatorItems: SpectatorItemsConfig = SpectatorItemsConfig(),
    @Comment("Configure the sounds that plays when a seeker is near")
    var seekerPing: SeekerPingConfig = SeekerPingConfig(),
    @Comment("For developers") var debug: Boolean = false,

    /* Timing */

    @Section("Timing")
    @Comment("How long in seconds will the game last, set to 0 to make game length infinite")
    var gameLength: ULong = 1200u,
    @Comment("How long in seconds will the initial hiding period last, minimum is 10 seconds")
    var hidingLength: ULong = 30u,
    @Comment(
        "The amount of seconds the game will wait until the players are teleported to the lobby after a game over"
    )
    var endGameDelay: ULong = 5u,
    @Comment(
        "If you die in game, you will have to wait [delay] seconds until you respawn, so that if you were a seeker,"
    )
    @Comment(
        "you cannot instantly go to where the Hider that killed you was. Or if you were a Hider and dies,"
    )
    @Comment("you can't instantly go to where you know other Hiders are. This can be disabled.")
    var delayedRespawn: DelayedRespawnConfig = DelayedRespawnConfig(),

    /* Database */

    @Section("Database") var database: DatabaseConfig = DatabaseConfig(),

    /* Scoring */

    @Section("Scoring")
    @Comment("The scoring mode decides the criteria for when the game has finished and who wins.")
    @Comment(
        "ALL_HIDERS_FOUND - The game will go until no hiders are left. If the timer runs out all hiders left will win."
    )
    @Comment(
        "LAST_HIDER_WINS - The game will go until there is only one hider left. If the timer runs out, all hiders left win. If there is only one hider left, all initial seekers win along with the last hider."
    )
    var scoringMode: ConfigScoringMode = ConfigScoringMode.ALL_HIDERS_FOUND,
    @Comment(
        "When enabled, if the last hider or seeker quits the game, a wine type of NONE is given, which doesn't mark anyone as winning."
    )
    @Comment(
        "This can be used as a way to prevent players from quitting in a loop to get someone else points."
    )
    var dontRewardQuit: Boolean = true,

    /* PVP */

    @Section("PVP")
    @Comment(
        "This plugin by default functions as not tag to catch Hiders, but to pvp. All players are given weapons,"
    )
    @Comment(
        "and seekers slightly better weapons (this can be changed in items.yml). If you want, you can disable this"
    )
    @Comment(
        "entire pvp functionality, and make Hiders get found on a single hit. Hiders would also not be able to fight"
    )
    @Comment("back against Seekers if disabled.")
    var pvp: Boolean = true,
    @Comment("Allow players to regen health") var regenHealth: Boolean = false,
    @Comment(
        "If pvp is disabled, Hiders and Seekers can no longer take damage from natural causes unless this option is enabled."
    )
    @Comment("Such natural causes could be fall damage or projectiles.")
    var allowNaturalCauses: Boolean = false,

    /* Lobby */

    @Section("Lobby")
    @Comment("Players that join the server will automatically be added into a game lobby")
    var autoJoin: Boolean = false,
    @Comment(
        "When players join the world containing the lobby, teleport them to the designated exit position so that they don't spawn in the lobby while not in the queue."
    )
    @Comment("This setting is ignored when autoJoin is set to true.")
    var teleportStraysToExit: Boolean = false,
    @Comment("How to handle players leaving a game lobby.")
    @Comment("EXIT - Teleport the player to the designated exit location")
    @Comment("PROXY - Teleport the player to another server in a bungeecord/velocity network")
    var leaveType: ConfigLeaveType = ConfigLeaveType.EXIT,
    @Comment("The server to teleport to when leaveType is set to PROXY")
    var leaveServer: String = "lobby",
    @Comment("If to leave the game lobby after a game ends") var leaveOnEnd: Boolean = false,
    @Comment("Configure the \"waiting for players\" per map lobby")
    var lobby: LobbyConfig = LobbyConfig(),
    @Comment("Restore the players previously cleared inventory after leaving the game lobby")
    var saveInventory: Boolean = false,

    /* Events */

    @Section("Events") @Comment("Taunt event") var taunt: TauntConfig = TauntConfig(),

    /* Power-ups */

    @Section("Power-ups") @Comment("Glow power-up") var glow: GlowConfig = GlowConfig(),
    @Comment(
        "Instead of having a glow power-up, always make seeker position's known the the hider at all times."
    )
    var alwaysGlow: Boolean = false,

    /* Protections */

    @Section("Protections")
    @Comment(
        "By default, the plugin forces you to use a map save to protect from changes to a map thought a game play though. It copies your"
    )
    @Comment(
        "hide-and-seek world to a separate world, and loads the game there to contain the game in an isolated and backed up map. This allows you to"
    )
    @Comment(
        "not worry about your hide-and-seek map from changing, as all changes are made are in a separate world file that doesn't get saved. Once the game"
    )
    @Comment(
        "ends, it unloads the map and doesn't save. Then reloads the duplicate to the original state, rolling back the map for the next game."
    )
    @Comment(
        "It is highly recommended that you keep this set to true unless you have other means of protecting your hide-and-seek map."
    )
    var mapSaveEnabled: Boolean = true,
    @Comment("Block these commands for players in a game. Good for blocking communication")
    var blockedCommands: List<String> = listOf("msg", "reply", "me", "kill"),
    @Comment("Dont allow players to interact with these blocks")
    var blockedInteracts: List<String> =
        listOf("FURNACE", "CRAFTING_TABLE", "ANVIL", "CHEST", "BARREL"),

    /* Auto Generated */

    @Section("Auto Generated")
    @Comment("Location where players are teleported to when they run (/hs leave).")
    var exit: Location? = null,
)
