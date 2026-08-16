package cat.freya.khs

import cat.freya.khs.config.ConfigGameMode
import cat.freya.khs.game.Game
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class TagTest : GameModeTest(ConfigGameMode.TAG) {
    @Test
    @DisplayName("PLAYERS_LEFT when no hiders")
    fun seekersWinWhenNoHiders() {
        startGame(false, alice.uuid)
        game.leave(bob.uuid)
        assertEquals(Game.WinType.PLAYERS_LEFT, game.gameMode.getWinCondition())
    }

    @Test
    @DisplayName("Seeker and hider swap teams on tag")
    fun seekerSwapsTeamWithHider() {
        startGame(true, alice.uuid)
        game.gameMode.handleDeath(bob, alice)
        assertEquals(Game.Team.HIDER, game.teams.get(alice.uuid))
        assertEquals(Game.Team.SEEKER, game.teams.get(bob.uuid))
    }
}
