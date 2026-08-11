package cat.freya.khs.game.gamemode

import cat.freya.khs.game.Game
import cat.freya.khs.world.Player
import java.util.UUID

class HideAndSeek(override val game: Game) : GameMode {
    private var lastHider: UUID? = null

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

        // dont reward quits (if enabled)
        val playerLeft = game.getPlayerLeft() && plugin.config.dontRewardQuit

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
            lastHider = game.teams.getHiders().firstOrNull()
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
}
