package cat.freya.khs.game

import cat.freya.khs.Khs
import java.util.UUID
import kotlin.math.roundToInt

const val DISABLED_IDENT = "KHS_DISABLED_FILTER_ME_OUT"

interface Board {
    fun setText(title: String, text: List<String>)

    fun display(uuid: UUID)

    interface Team {
        var prefix: String

        // options
        var canCollide: Boolean
        var nameTagsVisible: Boolean

        // players
        var players: Set<UUID>
    }

    // seeker/hider display
    fun getTeam(name: String): Board.Team
}

fun updateTeams(plugin: Khs, board: Board) {
    val hider = board.getTeam("Hider")
    val seeker = board.getTeam("Seeker")

    hider.players = plugin.game.hiderUUIDs
    seeker.players = plugin.game.seekerUUIDs

    hider.nameTagsVisible = plugin.config.nametagsVisible
    seeker.nameTagsVisible = plugin.config.nametagsVisible

    hider.canCollide = false
    seeker.canCollide = false

    hider.prefix = plugin.locale.game.team.hider
    seeker.prefix = plugin.locale.game.team.seeker
}

fun getLobbyBoard(plugin: Khs, uuid: UUID): Board? {
    return plugin.shim.getBoard("lobby-$uuid")
}

fun reloadLobbyBoard(plugin: Khs, uuid: UUID) {
    val timer = plugin.game.timer
    val countdown =
        when {
            timer != null -> plugin.boardConfig.countdown.startingIn.with(timer)
            else -> plugin.boardConfig.countdown.waiting
        }
    val count = plugin.game.size
    val seekerPercent = (plugin.game.getSeekerChance(uuid) * 100).roundToInt()
    val hiderPercent = 100 - seekerPercent
    val map = plugin.game.map?.name ?: ""

    val board = getLobbyBoard(plugin, uuid) ?: return
    updateTeams(plugin, board)

    val title = plugin.boardConfig.lobby.title
    board.setText(
        title,
        plugin.boardConfig.lobby.content.map {
            it.replace("{COUNTDOWN}", countdown)
                .replace("{COUNT}", count.toString())
                .replace("{SEEKER%}", seekerPercent.toString())
                .replace("{HIDER%}", hiderPercent.toString())
                .replace("{MAP}", map)
        },
    )
    board.display(uuid)
}

fun getGameBoard(plugin: Khs, uuid: UUID): Board? {
    return plugin.shim.getBoard("game-$uuid")
}

private fun getBorderLocale(plugin: Khs): String {
    val config = plugin.game.map?.config?.worldBorder
    val border = plugin.game.border

    if (config?.enabled != true || border.expired) return DISABLED_IDENT

    if (border.state == Border.State.SHRINKING) return plugin.boardConfig.border.shrinking

    val m = border.timer / 60UL
    val s = border.timer % 60UL
    return plugin.boardConfig.border.timer.with(m, s)
}

private fun getTauntLocale(plugin: Khs): String {
    val config = plugin.config.taunt
    val taunt = plugin.game.taunt

    if (!config.enabled || taunt.expired) return DISABLED_IDENT

    if (taunt.running) return plugin.boardConfig.taunt.active

    val m = taunt.timer / 60UL
    val s = taunt.timer % 60UL
    return plugin.boardConfig.taunt.timer.with(m, s)
}

private fun getGlowLocale(plugin: Khs): String {
    val config = plugin.config.glow
    val always = plugin.config.alwaysGlow
    val glow = plugin.game.glow

    if (always || !config.enabled) return DISABLED_IDENT

    if (glow.running) return plugin.boardConfig.glow.active
    else return plugin.boardConfig.glow.disabled
}

fun reloadGameBoard(plugin: Khs, uuid: UUID) {
    val timer = plugin.game.timer

    val time = plugin.boardConfig.countdown.timer.with((timer ?: 0UL) / 60UL, (timer ?: 0UL) % 60UL)
    val team =
        when (plugin.game.getTeam(uuid)) {
            Game.Team.HIDER -> plugin.locale.game.team.hider
            Game.Team.SEEKER -> plugin.locale.game.team.seeker
            else -> plugin.locale.game.team.spectator
        }

    // border event
    val border = getBorderLocale(plugin)
    val taunt = getTauntLocale(plugin)
    val glow = getGlowLocale(plugin)
    val numSeeker = plugin.game.seekerSize
    val numHider = plugin.game.hiderSize
    val map = plugin.game.map?.name ?: ""

    val board = getGameBoard(plugin, uuid) ?: return
    updateTeams(plugin, board)

    val title = plugin.boardConfig.game.title
    board.setText(
        title,
        plugin.boardConfig.game.content
            .map {
                it.replace("{TIME}", time)
                    .replace("{TEAM}", team)
                    .replace("{BORDER}", border)
                    .replace("{TAUNT}", taunt)
                    .replace("{GLOW}", glow)
                    .replace("{#SEEKER}", numSeeker.toString())
                    .replace("{#HIDER}", numHider.toString())
                    .replace("{MAP}", map)
            }
            .filter { !it.contains(DISABLED_IDENT) },
    )
    board.display(uuid)
}
