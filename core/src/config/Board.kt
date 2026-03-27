package cat.freya.khs.config

data class LobbyBoardConfig(
    var title: String = "&eHIDE AND SEEK",
    var content: List<String> =
        listOf(
            "{COUNTDOWN}",
            "",
            "Players: {COUNT}",
            "",
            "&cSEEKER % &f{SEEKER%}",
            "&6HIDER % &f{HIDER%}",
            "",
            "Map: {MAP}",
        ),
)

data class GameBoardConfig(
    var title: String = "&eHIDE AND SEEK",
    var content: List<String> =
        listOf(
            "Map: {MAP}",
            "Team: {TEAM}",
            "",
            "Time Left: &a{TIME}",
            "",
            "Taunt: &e{TAUNT}",
            "Glow: {GLOW}",
            "Border: &b{BORDER}",
            "",
            "&cSEEKERS: &f{#SEEKER}",
            "&6HIDERS: &f{#HIDER}",
        ),
)

data class CountdownBoardConfig(
    var waiting: String = "Waiting for players...",
    @Comment("{1} - time in seconds till game start")
    var startingIn: LocaleString1 = LocaleString1("Starting in: &a{1}s"),
    @Comment("{1} - how many minutes till game end")
    @Comment("{2} - how many seconds till game end")
    var timer: LocaleString2 = LocaleString2("{1}m{2}s"),
)

data class TauntBoardConfig(
    @Comment("{1} - number of minutes till taunt event")
    @Comment("{2} - number of seconds till taunt event")
    var timer: LocaleString2 = LocaleString2("{1}m{2}s"),
    var active: String = "Active",
)

data class GlowBoardConfig(var active: String = "&aActive", var disabled: String = "&cDisabled")

data class BorderBoardConfig(
    @Comment("{1} - number of minutes till border event")
    @Comment("{2} - number of seconds till border event")
    var timer: LocaleString2 = LocaleString2("{1}m{2}s"),
    var shrinking: String = "Shrinking",
)

data class KhsBoardConfig(
    @Section("Lobby")
    @Comment("Change what is displayed on the scoreboard/leaderboard")
    @Comment("while in the lobby")
    @Comment("")
    @Comment("  {COUNTDOWN} - Displays the time left until the game starts")
    @Comment("  {COUNT}     - The amount of players in the lobby")
    @Comment("  {SEEKER%}   - % chance that you will be a seeker")
    @Comment("  {HIDER%}    - % chance that you will be a hider")
    @Comment("  {MAP}       - The name of the current map")
    var lobby: LobbyBoardConfig = LobbyBoardConfig(),
    @Section("Game")
    @Comment("Change what is displayed on the scoreboard/leaderboard")
    @Comment("while playing the game")
    @Comment("")
    @Comment("  {TIME}      - The time left in the game (MM:SS)")
    @Comment("  {TEAM}      - The team you are on")
    @Comment("  {BORDER}    - The current status of the world border event")
    @Comment("  {TAUNT}     - The current status of the taunt event")
    @Comment("  {GLOW}      - The current status of the glow powerup")
    @Comment("  {#SEEKER}   - The number of seekers in the game right now")
    @Comment("  {#HIDER}    - The number of hiders in the game right now")
    @Comment("  {MAP}       - The name of the current map")
    var game: GameBoardConfig = GameBoardConfig(),
    @Section("Templates")
    @Comment("Locale strings for the {COUNTDOWN} display")
    var countdown: CountdownBoardConfig = CountdownBoardConfig(),
    @Comment("Locale strings for the {TAUNT} placeholder")
    var taunt: TauntBoardConfig = TauntBoardConfig(),
    @Comment("Locale strings for the {GLOW} placeholder")
    var glow: GlowBoardConfig = GlowBoardConfig(),
    @Comment("Locale strings for the {BORDER} placeholder")
    var border: BorderBoardConfig = BorderBoardConfig(),
)
