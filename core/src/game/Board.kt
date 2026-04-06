package cat.freya.khs.game

import cat.freya.khs.Khs
import cat.freya.khs.world.Player
import java.util.UUID
import kotlin.math.roundToInt

const val DISABLED_IDENT = "KHS_DISABLED_FILTER_ME_OUT"

/**
 * Wrapper for minecraft's scoreboard system I'ts called a Board since it doesnt really track scores
 * for us
 */
interface Board {
    interface Team {
        fun setPrefix(prefix: String)

        fun setCanCollide(canCollide: Boolean)

        fun setNameTagsVisible(nameTagsVisible: Boolean)

        fun setPlayers(players: Set<UUID>)
    }

    fun getTeam(name: String): Team

    fun setText(title: String, text: List<String>)
}

fun updateTeams(plugin: Khs, board: Board) {
    val hider = board.getTeam("Hider")
    val seeker = board.getTeam("Seeker")

    hider.setPlayers(plugin.game.hiderUUIDs)
    seeker.setPlayers(plugin.game.seekerUUIDs)

    hider.setNameTagsVisible(plugin.config.nametagsVisible)
    seeker.setNameTagsVisible(plugin.config.nametagsVisible)

    hider.setCanCollide(false)
    seeker.setCanCollide(false)

    hider.setPrefix(plugin.locale.game.team.hider)
    seeker.setPrefix(plugin.locale.game.team.seeker)
}

fun getLobbyBoard(plugin: Khs, uuid: UUID): Board? {
    return plugin.shim.getBoard("lobby-$uuid")
}

fun reloadLobbyBoard(plugin: Khs, player: Player) {
    val timer = plugin.game.timer
    val countdown =
        when {
            timer != null -> plugin.boardConfig.countdown.startingIn.with(timer)
            else -> plugin.boardConfig.countdown.waiting
        }
    val count = plugin.game.size
    val seekerPercent = (plugin.game.getSeekerChance(player.uuid) * 100).roundToInt()
    val hiderPercent = 100 - seekerPercent
    val map = plugin.game.map?.name ?: ""

    val board = getLobbyBoard(plugin, player.uuid) ?: return
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

    player.setScoreBoard(board)
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

    if (always || !config.enabled || !glow.supported) return DISABLED_IDENT

    return if (glow.running) plugin.boardConfig.glow.active else plugin.boardConfig.glow.disabled
}

fun reloadGameBoard(plugin: Khs, player: Player) {
    val timer = plugin.game.timer

    val time = plugin.boardConfig.countdown.timer.with((timer ?: 0UL) / 60UL, (timer ?: 0UL) % 60UL)
    val team =
        when (plugin.game.getTeam(player.uuid)) {
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

    val board = getGameBoard(plugin, player.uuid) ?: return
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

    player.setScoreBoard(board)
}
