package cat.freya.khs.game.gamemode

import cat.freya.khs.Khs
import cat.freya.khs.game.Game
import cat.freya.khs.world.Player
import java.util.UUID

interface GameMode {
    val game: Game

    val plugin: Khs
        get() = game.plugin

    /** check if a player is allowed to attack another player
     * in the game */
    fun isDamageAllowed(player: Player, attacker: Player?): Boolean {
        if (!game.teams.contains(player.uuid)) return false

        if (game.status != Game.Status.SEEKING) return false

        if (game.teams.isSpectator(player.uuid)) return false
        if (game.teams.isUnassigned(player.uuid)) return false

        if (attacker == null) {
            // assume natural causes
            if (!plugin.config.pvp && !plugin.config.allowNaturalCauses) return false

            return true
        }

        // attackers must be in the game to attack the player
        if (!game.teams.contains(attacker.uuid)) return false

        // spectators cannot attack
        if (game.teams.isSpectator(attacker.uuid)) return false
        if (game.teams.isUnassigned(attacker.uuid)) return false

        // players cannot attack their team-mates
        if (game.teams.get(player.uuid) == game.teams.get(attacker.uuid)) return false

        // ignore if pvp is disabled, and a hider is trying to attack a seeker
        if (!plugin.config.pvp && game.teams.isHider(attacker.uuid) && game.teams.isSeeker(player.uuid)) return false

        return true
    }

    /** a hider has been found/killed/tagged by a seeker */
    fun handleDeath(player: Player, attacker: Player?)

    /** check if the game should end now with a given win condition */
    fun getWinCondition(): Game.WinType?

    /** returns the last hider who won (if LAST_HIDER_WIN is the win condition) */
    fun getLastHider(): UUID? = null

    /** returns the effective team for the win condition for a player */
    fun getEffectiveTeam(uuid: UUID): Game.Team? {
        return game.teams.get(uuid)
    }
}
