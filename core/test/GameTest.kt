package cat.freya.khs

import cat.freya.khs.game.Game
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class GameTest : KhsTest() {
    @Test
    @DisplayName("Game cannot start when empty")
    fun gameCannotStartWhenEmpty() {
        setupMap()
        game.start()
        assertStatus(Game.Status.LOBBY)
    }

    @Test
    @DisplayName("Game cannot start with less then min players")
    fun gameCannotStartWithoutMinPlayers() {
        config.minPlayers = 2u
        setupMap()
        game.join(alice.uuid)
        game.start()
        assertStatus(Game.Status.LOBBY)
    }

    @Test
    @DisplayName("Game can start with atleast min players")
    fun gameCanStartWithMinPlayers() {
        config.minPlayers = 2u
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        assertEquals(2u, game.teams.size())
        game.start()
        assertStatus(Game.Status.HIDING)
    }

    @Test
    @DisplayName("Game hiding phase lasts `hidingLength` seconds")
    fun gameHidingPhaseLength() {
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        game.start()
        game.doTick()
        assertStatus(Game.Status.HIDING)
        skipSeconds(config.hidingLength - 1UL)
        assertStatus(Game.Status.HIDING)
        skipSeconds(1UL)
        assertStatus(Game.Status.SEEKING)
    }

    @Test
    @DisplayName("Game seeking phase lasts `gameLength` seconds")
    fun gameSeekingPhaseLength() {
        gameHidingPhaseLength()
        assertStatus(Game.Status.SEEKING)
        skipSeconds(config.gameLength - 1UL)
        assertStatus(Game.Status.SEEKING)
        skipSeconds(1UL)
        assertStatus(Game.Status.FINISHED)
    }

    @Test
    @DisplayName("Game finished phase lasts `endGameDelay` seconds")
    fun gameFinishedPhaseLength() {
        gameSeekingPhaseLength()
        assertStatus(Game.Status.FINISHED)
        skipSeconds(config.endGameDelay - 1UL)
        assertStatus(Game.Status.FINISHED)
        skipSeconds(1UL)
        assertStatus(Game.Status.LOBBY)
    }

    // make sure game.teams can acutally get players
    // as playerCache was breaking things

    @Test
    @DisplayName("Game status progresses in a defined order")
    fun gameStatusOrderDefined() {
        assertStatus(Game.Status.LOBBY)
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        game.start()
        game.doTick()
        assertStatus(Game.Status.HIDING)
        skipSeconds(config.hidingLength)
        assertStatus(Game.Status.SEEKING)
        skipSeconds(config.gameLength)
        assertStatus(Game.Status.FINISHED)
        skipSeconds(config.endGameDelay)
        assertStatus(Game.Status.LOBBY)
    }

    @Test
    @DisplayName("Starting a new game, there are only hiders and seekers")
    fun gameStartHasOnlyHidersOrSeekers() {
        assertStatus(Game.Status.LOBBY)
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        game.start()
        for (uuid in game.teams.getUUIDs()) {
            val team = game.teams.get(uuid)
            assertNotEquals(Game.Team.UNASSIGNED, team)
            assertNotEquals(Game.Team.SPECTATOR, team)
        }
    }

    @Test
    @DisplayName("Players are unassigned after game finishes")
    fun playersAreUnassignedAfterTheGameFinishes() {
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        game.start()
        skipToStatus(Game.Status.FINISHED)
        game.join(eve.uuid)
        skipToStatus(Game.Status.LOBBY)
        assertEquals(Game.Team.UNASSIGNED, game.teams.get(alice.uuid))
        assertEquals(Game.Team.UNASSIGNED, game.teams.get(bob.uuid))
        assertEquals(Game.Team.UNASSIGNED, game.teams.get(eve.uuid))
    }
}
