package cat.freya.khs

import cat.freya.khs.config.ConfigGameMode
import cat.freya.khs.config.ConfigScoringMode
import cat.freya.khs.game.Game
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class HideAndSeekTest : GameModeTest(ConfigGameMode.HIDE_AND_SEEK) {
    @Test
    @DisplayName("SEEKERS_WIN when no hiders")
    fun seekersWinWhenNoHiders() {
        startGame(false, alice.uuid)
        game.leave(bob.uuid)
        assertEquals(Game.WinType.SEEKERS_WIN, game.gameMode.getWinCondition())
    }

    @Test
    @DisplayName("LAST_HIDER_WIN when last hider left when LAST_HIDER_WINS scoring mode")
    fun lastHiderWins() {
        config.scoringMode = ConfigScoringMode.LAST_HIDER_WINS
        startGame(true, alice.uuid)
        assertEquals(Game.WinType.LAST_HIDER_WIN, game.gameMode.getWinCondition())
        assertEquals(bob.uuid, game.gameMode.getLastHider())
    }

    @Test
    @DisplayName("Hiders respawn as a seeker")
    fun hidersRespawnAsASeeker() {
        startGame(true, alice.uuid)
        game.gameMode.handleDeath(bob, alice)
        assertEquals(Game.Team.SEEKER, game.teams.get(bob.uuid))
    }

    @Test
    @DisplayName("Hiders respawn as a spectator (respawnAsSpectator)")
    fun hidersRespawnAsASpectator() {
        config.respawnAsSpectator = true
        startGame(true, alice.uuid)
        game.gameMode.handleDeath(bob, alice)
        assertEquals(Game.Team.SPECTATOR, game.teams.get(bob.uuid))
    }

    @Test
    @DisplayName("Seekers always respawn as a seeker")
    fun seekersAlwaysRespawnAsASeeker() {
        startGame(true, alice.uuid)
        game.gameMode.handleDeath(alice, null)
        assertEquals(Game.Team.SEEKER, game.teams.get(alice.uuid))
    }
}
