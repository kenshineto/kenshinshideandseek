package cat.freya.khs

import cat.freya.khs.game.Game
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class LobbyTest : KhsTest() {
    @Test
    @DisplayName("Players cannot join the lobby if a map isnt setup")
    fun playerJoinNotSetup() {
        game.join(alice.uuid)
        assertFalse(isMapSetup())
        assertFalse(game.teams.contains(alice.uuid))
    }

    @Test
    @DisplayName("Players can join the lobby if a map is setup")
    fun playerJoinSetup() {
        setupMap()
        game.join(alice.uuid)
        assertTrue(isMapSetup())
        assertTrue(game.teams.contains(alice.uuid))
    }

    @Test
    @DisplayName("Players can leave the game")
    fun playerLeave() {
        setupMap()
        game.join(alice.uuid)
        assertEquals(1u, game.teams.size())
        game.leave(alice.uuid)
        assertEquals(0u, game.teams.size())
    }

    @Test
    @DisplayName("Players leaving the game while not in it is a nop")
    fun playerLeaveNop() {
        setupMap()
        game.leave(bob.uuid)
        assertEquals(0u, game.teams.size())
    }

    @Test
    @DisplayName("Players leaving the game while not in it is a nop (2)")
    fun playerLeaveNop2() {
        setupMap()
        game.join(alice.uuid)
        game.leave(bob.uuid)
        assertEquals(1u, game.teams.size())
        game.leave(alice.uuid)
        assertEquals(0u, game.teams.size())
        game.leave(bob.uuid)
        assertEquals(0u, game.teams.size())
    }

    @Test
    @DisplayName("Players cannot join a full lobby")
    fun playerJoinFullLobby() {
        config.lobby.max = 1u
        setupMap()
        game.join(alice.uuid)
        assertEquals(1u, game.teams.size())
        game.join(bob.uuid)
        assertEquals(1u, game.teams.size())
    }

    @Test
    @DisplayName("Lobby wont start on its own")
    fun lobbyCannotSelfStart() {
        setupMap()
        assertThrows { skipToStatus(Game.Status.HIDING) }
    }

    @Test
    @DisplayName("Lobby can start with min lobby players")
    fun lobbyStartWithMinPlayers() {
        config.lobby.min = 2u
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        assertEquals(2u, game.teams.size())
        skipSeconds(config.lobby.countdown)
        assertStatus(Game.Status.HIDING)
    }

    @Test
    @DisplayName("Lobby quick start functions")
    fun lobbyQuickStart() {
        config.lobby.min = 2u
        config.lobby.changeCountdown = 3u
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        assertEquals(2u, game.teams.size())
        skipSeconds(10UL)
        assertStatus(Game.Status.LOBBY)
    }

    @Test
    @DisplayName("Lobby quick start functions (2)")
    fun lobbyQuickStart2() {
        config.lobby.min = 2u
        config.lobby.changeCountdown = 2u
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        assertEquals(2u, game.teams.size())
        skipSeconds(10UL)
        assertStatus(Game.Status.HIDING)
    }

    @Test
    @DisplayName("Lobby timer doesnt count down when empty")
    fun lobbyTimerDoesntCountDownWhenEmpty() {
        val timer = game.timer
        skipSeconds(5UL)
        assertEquals(timer, game.timer)
    }

    @Test
    @DisplayName("Lobby timer doesnt count down without min players")
    fun lobbyTimerDoesntCountDownWithoutMinPlayers() {
        val timer = game.timer
        setupMap()
        game.join(alice.uuid)
        assertEquals(1u, game.teams.size())
        skipSeconds(5UL)
        assertEquals(timer, game.timer)
    }

    @Test
    @DisplayName("Lobby timer does count down with min players")
    fun lobbyTimerDoesCountDownWithMinPlayers() {
        val timer = game.timer
        config.lobby.min = 2u
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        assertEquals(2u, game.teams.size())
        skipSeconds(5UL)
        assertNotEquals(timer, game.timer)
        assertNotEquals(timer, config.lobby.countdown)
    }

    @Test
    @DisplayName("Players are unassigned in the lobby")
    fun playersAreUnassignedInTheLobby() {
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        assertEquals(Game.Team.UNASSIGNED, game.teams.get(alice.uuid))
        assertEquals(Game.Team.UNASSIGNED, game.teams.get(bob.uuid))
    }

    @Test
    @DisplayName("Players are teleported to the lobby upon joining")
    fun playersAreTeleportedToTheLobbyUponJoining() {
        val map = setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        assertEquals(map.lobbySpawn, alice.getLocation())
        assertEquals(map.lobbySpawn, bob.getLocation())
    }
}
