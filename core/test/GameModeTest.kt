package cat.freya.khs

import cat.freya.khs.config.ConfigGameMode
import cat.freya.khs.game.Game
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

abstract class GameModeTest(val gameMode: ConfigGameMode) : KhsTest() {
    protected fun startGame(rewardQuit: Boolean, seeker: UUID? = null) {
        config.gameMode = gameMode
        config.dontRewardQuit = rewardQuit
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        if (seeker != null) {
            game.start(setOf(seeker))
        } else {
            game.start()
        }
        game.join(eve.uuid)
        assertStatus(Game.Status.HIDING)
    }

    @Test
    @DisplayName("HIDERS_WIN when timer runs out")
    fun hidersWinWhenTimerRunsOut() {
        startGame(true)
        game.doTick()
        skipSeconds(config.hidingLength)
        skipSeconds(config.gameLength)
        assertEquals(Game.WinType.HIDERS_WIN, game.gameMode.getWinCondition())
    }

    @Test
    @DisplayName("PLAYERS_LEFT when seeker quit and dontRewardQuit")
    fun playersLeftWhenSeekerQuitAndDontRewardQuit() {
        startGame(true, alice.uuid)
        game.leave(alice.uuid)
        assertEquals(Game.WinType.PLAYERS_LEFT, game.gameMode.getWinCondition())
    }

    @Test
    @DisplayName("PLAYERS_LEFT when hider quit and dontRewardQuit")
    fun playersLeftWhenHiderQuitAndDontRewardQuit() {
        startGame(true, alice.uuid)
        game.leave(bob.uuid)
        assertEquals(Game.WinType.PLAYERS_LEFT, game.gameMode.getWinCondition())
    }

    @Test
    @DisplayName("HIDERS_WIN when no seekers")
    fun hidersWinWhenNoSeekers() {
        startGame(false, alice.uuid)
        game.leave(alice.uuid)
        assertEquals(Game.WinType.HIDERS_WIN, game.gameMode.getWinCondition())
    }

    @Test
    @DisplayName("Nobody can interact if not seeking")
    fun nobodyCanInteractIfNotSeeking() {
        startGame(false, alice.uuid)
        game.leave(alice.uuid)
        assertFalse(game.gameMode.isDamageAllowed(alice, bob))
        assertFalse(game.gameMode.isDamageAllowed(bob, alice))
    }

    @Test
    @DisplayName("Players cannot attack their teammates")
    fun playersCannotAttackTheirTeammates() {
        startGame(false, alice.uuid)
        skipToStatus(Game.Status.SEEKING)
        game.loadSeeker(mallory)
        assertEquals(Game.Team.SEEKER, game.teams.get(mallory.uuid))
        assertFalse(game.gameMode.isDamageAllowed(alice, mallory))
        game.loadHider(mallory)
        assertEquals(Game.Team.HIDER, game.teams.get(mallory.uuid))
        assertFalse(game.gameMode.isDamageAllowed(bob, mallory))
    }

    @Test
    @DisplayName("Seekers cannot interact with players")
    fun seekersCannotInteractWithPlayers() {
        startGame(true, alice.uuid)
        skipToStatus(Game.Status.SEEKING)
        assertEquals(Game.Team.SPECTATOR, game.teams.get(eve.uuid))
        assertFalse(game.gameMode.isDamageAllowed(alice, eve))
        assertFalse(game.gameMode.isDamageAllowed(eve, alice))
        assertFalse(game.gameMode.isDamageAllowed(bob, eve))
        assertFalse(game.gameMode.isDamageAllowed(eve, bob))
    }

    @Test
    @DisplayName("Players not in game cannot interact with players")
    fun playersNotInGameCannotInteractWithPlayers() {
        startGame(true, alice.uuid)
        skipToStatus(Game.Status.SEEKING)
        assertFalse(game.teams.contains(mallory.uuid))
        assertFalse(game.gameMode.isDamageAllowed(alice, mallory))
        assertFalse(game.gameMode.isDamageAllowed(mallory, alice))
        assertFalse(game.gameMode.isDamageAllowed(bob, mallory))
        assertFalse(game.gameMode.isDamageAllowed(mallory, bob))
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    @DisplayName("Hiders can attack seekers if pvp is enabled")
    fun hidersCanAttackSeekersIfPvpIsEnabled(pvpEnabled: Boolean) {
        config.pvp = pvpEnabled
        startGame(true, alice.uuid)
        skipToStatus(Game.Status.SEEKING)
        assertEquals(pvpEnabled, game.gameMode.isDamageAllowed(alice, bob))
    }
}
