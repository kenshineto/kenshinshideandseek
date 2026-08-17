package cat.freya.khs

import cat.freya.khs.game.Game
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

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
        setupMap()
        game.join(alice.uuid)
        game.start()
        assertStatus(Game.Status.LOBBY)
    }

    @Test
    @DisplayName("Game can start with atleast min players")
    fun gameCanStartWithMinPlayers() {
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

    @Test
    @DisplayName("Game status is LOBBY on startup")
    fun gameStatusIsLobbyOnStartup() {
        assertStatus(Game.Status.LOBBY)
    }

    @Test
    @DisplayName("Map is null on startup")
    fun gameMapIsNullOnStartup() {
        assertNull(game.map)
    }

    @Test
    @DisplayName("Can set map when a game is not in progress")
    fun gameCanSetMapWhenAGameIsNotInProgress() {
        val map = setupMap()
        game.setMap(map)
        assertEquals(map, game.map)
    }

    @Test
    @DisplayName("Can set game map to null")
    fun gameCanSetGameMapToNull() {
        gameCanSetMapWhenAGameIsNotInProgress()
        game.setMap(null)
        assertNull(game.map)
    }

    @Test
    @DisplayName("Cannot set map when a game is in progress")
    fun gameCannotSetMapWhenAGameIsInProgress() {
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        game.start()
        assertStatus(Game.Status.HIDING)
        game.setMap(null)
        assertNotNull(game.map)
    }

    @Test
    @DisplayName("Cannot set map to null when lobby has players")
    fun gameCannotSetMapToNullWhenLobbyHasPlayers() {
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        assertStatus(Game.Status.LOBBY)
        game.setMap(null)
        assertNotNull(game.map)
    }

    @Test
    @DisplayName("Players have no items during hiding phase")
    fun playersHaveNoItemsDuringHidingPhase() {
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        game.start()
        assertStatus(Game.Status.HIDING)
        assertTrue(alice.getInventory().getContents().all { it == null })
        assertTrue(bob.getInventory().getContents().all { it == null })
    }

    @ParameterizedTest
    @CsvSource("true, true", "true, false", "false, true", "false, false")
    @DisplayName("Hiders have correct items")
    fun hidersHaveCorrectItems(glowEnabled: Boolean, pvpEnabled: Boolean) {
        config.glow.enabled = glowEnabled
        config.pvp = pvpEnabled
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        game.start(setOf(alice.uuid))
        skipToStatus(Game.Status.SEEKING)
        assertTrue(game.teams.isHider(bob.uuid))

        val contents = bob.getInventory().getContents().filterNotNull()
        itemsConfig.hiderItems.forEach { itemConfig ->
            val item = contents.firstOrNull { it.similar(itemConfig) }
            if (pvpEnabled) {
                assertNotNull(item)
            } else {
                assertNull(item)
            }
        }

        val glow = contents.firstOrNull { it.similar(config.glow.item) }
        if (config.glow.enabled) {
            assertNotNull(glow)
        } else {
            assertNull(glow)
        }
    }

    @ParameterizedTest
    @DisplayName("Seekers have correct items")
    @ValueSource(booleans = [true, false])
    fun seekeresHaveCorrectItems(pvpEnabled: Boolean) {
        config.pvp = pvpEnabled
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        game.start(setOf(alice.uuid))
        skipToStatus(Game.Status.SEEKING)
        assertTrue(game.teams.isSeeker(alice.uuid))

        val contents = alice.getInventory().getContents().filterNotNull()
        itemsConfig.seekerItems.forEach { itemConfig ->
            val item = contents.firstOrNull { it.similar(itemConfig) }
            if (pvpEnabled) {
                assertNotNull(item)
            } else {
                assertNull(item)
            }
        }
    }

    @Test
    @DisplayName("Hiders are teleported to the spawn on start")
    fun hidersAreTeleportedToTheSpawnOnStart() {
        val map = setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        game.start(setOf(alice.uuid))

        val location = bob.getLocation()
        assertEquals(map.gameSpawn, location)

        // move bob
        location.x += 50.0
        location.z -= 50.0
        bob.teleport(location)

        skipToStatus(Game.Status.SEEKING)
        assertEquals(location, bob.getLocation())
    }

    @Test
    @DisplayName("Seekers are teleported to the seeker lobby on start")
    fun seekersAreTeleportedToTheSeekerLobbyOnStart() {
        val map = setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        game.start(setOf(alice.uuid))
        assertEquals(map.seekerLobbySpawn, alice.getLocation())
        skipToStatus(Game.Status.SEEKING)
        assertEquals(map.gameSpawn, alice.getLocation())
    }

    @Test
    @DisplayName("Spectators are teleported to the game spawn upon join")
    fun spectatorsAreTeleportedToTheGameSpawnUponJoin() {
        val map = setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        game.start()
        game.join(eve.uuid)
        assertEquals(map.gameSpawn, eve.getLocation())
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    @DisplayName("Seeker is teleported to the correct location upon death")
    fun seekerIsTeleportedToTheCorrectLocationUponDeath(delayedRespawnEnabled: Boolean) {
        config.delayedRespawn.enabled = delayedRespawnEnabled
        val map = setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        game.start(setOf(alice.uuid))
        skipToStatus(Game.Status.SEEKING)

        // move alice
        val location = alice.getLocation()
        location.x += 30.0
        location.x -= 30.0
        alice.teleport(location)

        // respawn
        game.loadSeeker(alice, true)
        if (delayedRespawnEnabled) {
            assertEquals(map.seekerLobbySpawn, alice.getLocation())
        } else {
            assertEquals(map.gameSpawn, alice.getLocation())
        }
    }
}
