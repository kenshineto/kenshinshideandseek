package cat.freya.khs.config

@JvmInline
value class LocaleString1(val inner: String) {
    fun with(arg1: Any): String {
        return this.inner.replace("{1}", arg1.toString())
    }

    override fun toString(): String = "[LocaleString1]"
}

@JvmInline
value class LocaleString2(val inner: String) {
    fun with(arg1: Any, arg2: Any): String {
        return this.inner.replace("{1}", arg1.toString()).replace("{2}", arg2.toString())
    }

    override fun toString(): String = "[LocaleString2]"
}

@JvmInline
value class LocaleString3(val inner: String) {
    fun with(arg1: Any, arg2: Any, arg3: Any): String {
        return this.inner
            .replace("{1}", arg1.toString())
            .replace("{2}", arg2.toString())
            .replace("{3}", arg3.toString())
    }

    override fun toString(): String = "[LocaleString3]"
}

data class LocalePrefixConfig(
    var default: String = "&9Hide and Seek > &f",
    var warning: String = "&eWarning > &f",
    var error: String = "&cError > &f",
    var abort: String = "&cAbort > &f",
    var taunt: String = "&eTaunt > &f",
    var border: String = "&cWorld Border > &f",
    var gameOver: String = "&aGame Over > &f",
)

data class LocalePlaceholderConfig(
    @Comment("Displayed string if the requested placeholder is invalid")
    var invalid: String = "{Error}",
    @Comment("Displayed string if the requested placeholder is empty") var noData: String = "-",
)

data class LocaleCommandConfig(
    var playerOnly: String = "This command can only be run by a player",
    var notAllowed: String = "You are not allowed to run this command",
    var notAllowedTemp: String = "You are not allowed to run this command right now",
    var unknownError: String = "An unknown error has occurred",
    @Comment("{1} - position of invalid argument")
    var invalidArgument: LocaleString1 = LocaleString1("Invalid argument: {1}"),
    var notEnoughArguments: String = "This command requires more arguments to run",
    @Comment("{1} - the invalid integer")
    var invalidInteger: LocaleString1 = LocaleString1("Invalid integer: {1}"),
    @Comment("{1} - the invalid player name")
    var invalidPlayer: LocaleString1 = LocaleString1("Invalid player: {1}"),
    var reloading: String = "Reloading the config...",
    var reloaded: String = "Reloaded the config",
    var errorReloading: String = "Error reloading config, please check the server logs!",
    @Comment("{1} - the bungeecord server name")
    var sendToServerFailed: LocaleString1 = LocaleString1("Cannot teleport you to server: {1}"),
)

data class LocaleGamePlayerConfig(
    @Comment("{1} - name of the player who died")
    var death: LocaleString1 = LocaleString1("&c{1}&f was killed"),
    @Comment("{1} - name of the hider who was found")
    var found: LocaleString1 = LocaleString1("&e{1}&f was found"),
    @Comment("{1} - name of the hider who was found")
    @Comment("{2} - name of the seeker who found the hider")
    var foundBy: LocaleString2 = LocaleString2("&e{1}&f was found by &c{2}&f"),
)

data class LocaleGameGameOverConfig(
    var hidersFound: String = "All hiders have been found",
    @Comment("{1} - the name of the last hider")
    var lastHider: LocaleString1 = LocaleString1("The last hider, &e{1}&f, has won!"),
    var seekerQuit: String = "All seekers have quit",
    var hiderQuit: String = "All hiders have quit",
    var time: String = "Seekers have run out of time. Hiders win!",
)

data class LocaleGameTitleConfig(
    var hidersWin: String = "&aHiders Win!",
    @Comment("{1} - the name of the hider who won")
    var singleHiderWin: LocaleString1 = LocaleString1("&a{1} Wins!"),
    var singleHiderWinSubtitle: LocaleString1 = LocaleString1("{1} is the last hider alive!"),
    var seekersWin: String = "&cSeekers Win!",
    var noWin: String = "&bGame Over",
)

data class LocaleGameCountdownConfig(
    @Comment("{1} - the amount of seconds hiders have left to hide")
    var notify: LocaleString1 = LocaleString1("Hiders have {1} seconds left to hide!"),
    var last: String = "Hiders have 1 second left to hide",
)

data class LocaleGameTeamConfig(
    var hider: String = "&6&lHIDER &r",
    var seeker: String = "&c&lSEEKER &r",
    var spectator: String = "&8&lSPECTATOR",
    var hiderSubtitle: String = "Hide from the seekers",
    var seekerSubtitle: String = "Find the hiders",
    var spectatorSubtitle: String = "You've joined mid-game",
)

data class LocaleGameConfig(
    var player: LocaleGamePlayerConfig = LocaleGamePlayerConfig(),
    var gameOver: LocaleGameGameOverConfig = LocaleGameGameOverConfig(),
    var title: LocaleGameTitleConfig = LocaleGameTitleConfig(),
    var countdown: LocaleGameCountdownConfig = LocaleGameCountdownConfig(),
    var team: LocaleGameTeamConfig = LocaleGameTeamConfig(),
    var setup: String =
        "There are no maps setup! Run /hs map status on a map to see what you need to do",
    var inGame: String = "You are already in the lobby/game",
    var notInGame: String = "You are not in a lobby/game",
    var inProgress: String = "There is currently a game in progress",
    var notInProgress: String = "There is no game in progress",
    var join: String = "You have joined mid game and are not a spectator",
    @Comment("{1} - the name of the player who left the game")
    var leave: LocaleString1 = LocaleString1("{1} has left the game"),
    var start: String = "Attention SEEKERS, it's time to find the hiders!",
    var stop: String = "The game has been forcefully stopped",
    @Comment("{1} - the time till respawn")
    var respawn: LocaleString1 = LocaleString1("You will respawn in {1} seconds"),
)

data class LocaleSpectatorConfig(
    var flyingEnabled: String = "&l&bFlying enabled",
    var flyingDisabled: String = "&l&bFlying disabled",
)

data class LocaleLobbyConfig(
    @Comment("{1} - the name of the player who joined the lobby")
    var join: LocaleString1 = LocaleString1("{1} has joined the lobby"),
    @Comment("{1} - the name of the player who left the lobby")
    var leave: LocaleString1 = LocaleString1("{1} has left the lobby"),
    var inUse: String = "Can't modify the lobby while players are in it",
    var full: String = "You cannot join the lobby since it is full",
    @Comment("{1} - the minimum number of players required to start the game")
    var notEnoughPlayers: LocaleString1 =
        LocaleString1("You must have at least {1} players to start"),
)

data class LocaleMapSaveConfig(
    var start: String = "Starting map save",
    var warning: String =
        "All commands will be disabled when the save is in progress. Do not turn of the server.",
    var inProgress: String = "Map save is currently in progress! Try again later.",
    var finished: String = "Map save complete",
    @Comment("{1} - the error message")
    var failed: LocaleString1 = LocaleString1("Map save failed with the following error: {1}"),
    var failedLocate: String = "Map save failed. Could not locate the map to save!",
    var failedLoad: String = "Map save failed. Could not load the map!",
    @Comment("{1} - the name of the directory that could not be renamed")
    var failedDir: LocaleString1 = LocaleString1("Failed to rename/delete directory: {1}"),
    var disabled: String = "Map saves are disabled in config.yml",
)

data class LocaleMapSetupConfig(
    @Comment("{1} - the map that is not yet setup")
    var not: LocaleString1 = LocaleString1("Map {1} is not setup (/hs map status <map>)"),
    var header: String = "&f&lThe following is needed for setup...",
    var game: String = "&c&l- &fGame spawn isn't setup, /hs map set spawn <map>",
    var lobby: String = "&c&l- &fLobby spawn isn't setup, /hs map set lobby <map>",
    var seekerLobby: String =
        "&c&l- &fSeeker Lobby spawn isn't setup, /hs map set seekerLobby <map>",
    var exit: String = "&c&l- &fQuit/exit teleport location isn't set, /hs setexit",
    var saveMap: String = "&c&l- &FMap isn't saved, /hs map save <map>",
    var bounds: String =
        "&c&l- &fPlease set game bounds in 2 opposite corners of the game map, /hs map set bounds <map>",
    var blockHunt: String =
        "&c&l - &fSince block hunt is enabled, there needs to be at least 1 block set, /hs map blockHunt block add block <map> <block>",
    var complete: String = "Everything is setup and ready to go!",
)

data class LocaleMapErrorConfig(
    var locationNotSet: String =
        "This location is not set (run /hs map status <map> for more info)",
    var notInRange: String = "This position is out of range (check bounds or world border)",
    var bounds: String = "Please set map bounds first",
)

data class LocaleMapWarnConfig(
    var gameSpawnReset: String = "Game spawn has been reset due to being out of range",
    var seekerSpawnReset: String = "Seeker spawn has been reset due to being out of range",
    var lobbySpawnReset: String = "Lobby spawn has been reset due to being out of range",
)

data class LocaleMapSetConfig(
    var gameSpawn: String = "Set game spawn position to your current position",
    var seekerSpawn: String = "Set seeker spawn position to your current position",
    var lobby: String = "Set lobby position to your current position",
    var exit: String = "Set exit position to your current position",
    @Comment("{1} - if the 1st or 2nd bound position was set")
    var bounds: LocaleString1 =
        LocaleString1("Successfully set bounds at your current position ({1}/2)"),
)

data class LocaleMapConfig(
    var save: LocaleMapSaveConfig = LocaleMapSaveConfig(),
    var setup: LocaleMapSetupConfig = LocaleMapSetupConfig(),
    var error: LocaleMapErrorConfig = LocaleMapErrorConfig(),
    var warn: LocaleMapWarnConfig = LocaleMapWarnConfig(),
    var set: LocaleMapSetConfig = LocaleMapSetConfig(),
    var list: String = "The current maps are:",
    var none: String = "There are no maps known to the plugin (/hs map add <name> <world>)",
    var noneSetup: String = "There are no maps setup and ready to play",
    var invalidName: String = "A map name can only contain ascii numbers and letters",
    var wrongWorld: String = "Please run this command in the game world",
    var exists: String = "A map with this name already exists!",
    var unknown: String = "That map does not exist",
    @Comment("{1} - the name of the new map")
    var created: LocaleString1 = LocaleString1("Created map: {1}"),
    @Comment("{1} - the name of the deleted map")
    var deleted: LocaleString1 = LocaleString1("Deleted map: {1}"),
)

data class LocaleWorldBorderConfig(
    var disable: String = "Disabled world border",
    var minSize: String = "World border cannot be smaller than 100 blocks",
    var minChange: String = "World border move be able to move",
    var position: String = "Spawn position must be 100 from world border center",
    @Comment("{1} - the new size of the world border")
    @Comment("{2} - the new delay of the world border")
    @Comment("{3} - how much the border changes at a time")
    var enable: LocaleString3 =
        LocaleString3(
            "Set border center to current location, size to {1}, delay to {2}, and steps by {3} blocks",
        ),
    var warn: String = "World border will shrink in the next 30s!",
    var shrinking: String = "&c&oWorld border is shrinking!",
)

data class LocaleTauntConfig(
    var chosen: String = "&c&oOh no! You have been chosen to be taunted",
    var warning: String = "A random hider will be taunted in the next 30s",
    var activate: String = "Taunt has been activated",
)

data class LocaleBlockHuntBlockConfig(
    @Comment("{1} - the block trying to be added to the block hunt map")
    var exists: LocaleString1 = LocaleString1("{1} has already been added to this map"),
    @Comment("{1} - the block trying to be removed from the block hunt map")
    var doesntExist: LocaleString1 = LocaleString1("{1} is already not used for the map"),
    @Comment("{1} - the block added to the block hunt map")
    var added: LocaleString1 = LocaleString1("Added {1} as a disguise to the map"),
    @Comment("{1} - the block removed from the block hunt map")
    var removed: LocaleString1 = LocaleString1("Removed {1} as a disguise from the map"),
    var list: String = "The block disguises for the map are:",
    var none: String = "There are no block disguises in use for this map",
    var unknown: String = "This block name does not exist",
)

data class LocaleBlockHuntConfig(
    var notEnabled: String = "Block hunt is not enabled on ths map",
    var notSupported: String = "Block hunt does not work on 1.8",
    var enabled: String = "Block hunt has been enabled",
    var disabled: String = "Block hunt has been disabled",
    var block: LocaleBlockHuntBlockConfig = LocaleBlockHuntBlockConfig(),
)

data class LocaleWorldConfig(
    @Comment("{1} - the world name")
    var exists: LocaleString1 = LocaleString1("A world named {1} already exists"),
    @Comment("{1} - the world name")
    var doesntExist: LocaleString1 = LocaleString1("There is not world named {1}"),
    @Comment("{1} - the world name")
    var added: LocaleString1 = LocaleString1("Created a world named {1}"),
    var addedFailed: LocaleString1 = LocaleString1("Failed to create a world named {1}"),
    @Comment("{1} - the world name")
    var removed: LocaleString1 = LocaleString1("Removed the world named {1}"),
    var removedFailed: LocaleString1 = LocaleString1("Failed to remove the world named {1}"),
    @Comment("{1} - the world name")
    @Comment("{2} - the map using the world")
    var inUseBy: LocaleString2 = LocaleString2("The world {1} is in use by map {2}"),
    var inUse: LocaleString1 = LocaleString1("The world {1} is in use by the plugin"),
    @Comment("{1} - the world name")
    var loadFailed: LocaleString1 = LocaleString1("Failed to load: {1}"),
    @Comment("{1} - the given world type")
    var invalidType: LocaleString1 = LocaleString1("Invalid world type: {1}"),
    var notEmpty: String = "World must be empty to be deleted",
    var list: String = "The following worlds are",
    var none: String = "Failed to fetch any worlds",
)

data class LocaleDatabaseConfig(
    var noInfo: String = "No gameplay info",
    @Comment("{1} - the player associated with the following win information")
    var infoFor: LocaleString1 = LocaleString1("Win information for {1}:"),
)

data class LocaleConfirmConfig(
    var none: String = "You have nothing to confirm",
    var timedOut: String = "The confirmation has timed out",
    var confirm: String = "Run /hs confirm within 10s to confirm",
)

data class KhsLocale(
    @Section("Language") @Comment("What language is this for?") var locale: String = "en_US",
    @Section("Message prefixes")
    @Comment("Specify prefixes for plugin chat messages.")
    var prefix: LocalePrefixConfig = LocalePrefixConfig(),
    @Section("Placeholder errors")
    @Comment("PlaceholderAPI error strings")
    var placeholder: LocalePlaceholderConfig = LocalePlaceholderConfig(),
    @Section("Command responses") var command: LocaleCommandConfig = LocaleCommandConfig(),
    @Section("Gameplay") var game: LocaleGameConfig = LocaleGameConfig(),
    @Section("Spectator") var spectator: LocaleSpectatorConfig = LocaleSpectatorConfig(),
    @Section("Lobby") var lobby: LocaleLobbyConfig = LocaleLobbyConfig(),
    @Section("Map") var map: LocaleMapConfig = LocaleMapConfig(),
    @Section("World Border") var worldBorder: LocaleWorldBorderConfig = LocaleWorldBorderConfig(),
    @Section("Taunt event") var taunt: LocaleTauntConfig = LocaleTauntConfig(),
    @Section("Block Hunt") var blockHunt: LocaleBlockHuntConfig = LocaleBlockHuntConfig(),
    @Section("World") var world: LocaleWorldConfig = LocaleWorldConfig(),
    @Section("Database") var database: LocaleDatabaseConfig = LocaleDatabaseConfig(),
    @Section("Confirm") var confirm: LocaleConfirmConfig = LocaleConfirmConfig(),
)
