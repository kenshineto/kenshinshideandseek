package cat.freya.khs.game.gamemode

import cat.freya.khs.game.Game
import cat.freya.khs.world.Player
import java.util.UUID

class HideAndSeek(override val game: Game) : GameMode {
    private var lastHider: UUID? = null
    private var lastHiderName: String? = null

    private fun respawnPlayer(player: Player) {
        if (game.teams.isHider(player.uuid) && plugin.config.respawnAsSpectator) {
            game.loadSpectator(player)
            return
        }

        // respawn as a seeker
        game.loadSeeker(player, true)
    }

    private fun broadcastDeath(player: Player, attacker: Player?) {
        val msg =
            if (game.teams.isSeeker(player.uuid)) {
                plugin.locale.game.player.death.with(player.name)
            } else if (attacker == null) {
                plugin.locale.game.player.found.with(player.name)
            } else {
                plugin.locale.game.player.foundBy.with(player.name, attacker.name)
            }

        game.broadcast(msg)
    }

    override fun handleDeath(player: Player, attacker: Player?) {
        // play death sound
        val soundName = if (plugin.shim.supports(9)) "ENTITY_PLAYER_DEATH" else "ENTITY_PLAYER_HURT"
        player.getWorld()?.playSound(player.getPosition(), soundName, 1.0, 1.0)

        // update leaderboard
        game.addDeath(player.uuid)
        if (attacker != null) game.addKill(attacker.uuid)

        broadcastDeath(player, attacker)
        respawnPlayer(player)
    }

    override fun getWinCondition(): Game.WinType? {
        val minHiders = plugin.config.scoringMode.minHiders

        // reset last hider field
        lastHider = null
        lastHiderName = null

        // dont reward quits (if enabled)
        val playerLeft = game.hasPlayerLeft() && plugin.config.dontRewardQuit

        if (game.timer == 0UL) {
            return Game.WinType.HIDERS_WIN
        }

        if (game.teams.seekerCount() < 1u) {
            if (playerLeft) {
                return Game.WinType.PLAYERS_LEFT
            } else {
                return Game.WinType.HIDERS_WIN
            }
        }

        if (game.teams.hiderCount() < minHiders) {
            val hider = game.teams.getHiderPlayers().firstOrNull()
            lastHider = hider?.uuid
            lastHiderName = hider?.name
            if (playerLeft) {
                return Game.WinType.PLAYERS_LEFT
            } else if (lastHider != null) {
                return Game.WinType.LAST_HIDER_WIN
            } else {
                return Game.WinType.SEEKERS_WIN
            }
        }

        return null
    }

    override fun getLastHider(): UUID? {
        return lastHider
    }

    override fun getEffectiveTeam(uuid: UUID): Game.Team? {
        return game.getInitialTeams().get(uuid)
    }

    override fun getMinPlayers(): UInt {
        return plugin.config.scoringMode.minHiders + 1u
    }

    override fun gameOverTitle(reason: Game.WinType): String {
        return when (reason) {
            Game.WinType.SEEKERS_WIN -> plugin.locale.game.title.seekersWin
            Game.WinType.HIDERS_WIN -> plugin.locale.game.title.hidersWin
            Game.WinType.LAST_HIDER_WIN -> plugin.locale.game.title.singleHiderWin.with(lastHiderName ?: "null")
            else -> plugin.locale.game.title.noWin
        }
    }

    override fun gameOverMessage(reason: Game.WinType): String {
        return when (reason) {
            Game.WinType.STOPPED -> plugin.locale.game.stop
            Game.WinType.PLAYERS_LEFT -> plugin.locale.game.gameOver.playersQuit
            Game.WinType.SEEKERS_WIN -> plugin.locale.game.gameOver.hidersFound
            Game.WinType.HIDERS_WIN -> plugin.locale.game.gameOver.time
            Game.WinType.LAST_HIDER_WIN -> plugin.locale.game.gameOver.lastHider.with(lastHiderName ?: "null")
        }
    }
}
