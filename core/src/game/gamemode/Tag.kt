package cat.freya.khs.game.gamemode

import cat.freya.khs.game.Game
import cat.freya.khs.world.Player

class Tag(override val game: Game) : GameMode {
    private fun respawnPlayer(player: Player) {
        when (game.teams.get(player.uuid)) {
            Game.Team.HIDER -> game.loadSeeker(player)
            Game.Team.SEEKER -> game.loadSeeker(player)
            else -> {}
        }
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
        // tag MUST always have an attacker
        val attacker = attacker ?: game.teams.getSeekerPlayers().firstOrNull() ?: return
        broadcastDeath(player, attacker)

        // play death sound
        player.getWorld()?.playSound(player.getPosition(), "BLOCK_ANVIL_LAND", 1.0, 1.0)

        // update leaderboard
        game.addDeath(player.uuid)
        game.addKill(attacker.uuid)

        respawnPlayer(player)
        respawnPlayer(attacker)
    }

    override fun getWinCondition(): Game.WinType? {
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

        if (game.teams.hiderCount() < 1u) {
            // seekers are never allowed to win
            // in tag
            return Game.WinType.PLAYERS_LEFT
        }

        return null
    }
}
