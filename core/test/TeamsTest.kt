package cat.freya.khs

import cat.freya.khs.game.Game
import cat.freya.khs.world.Player
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class TeamsTest : KhsTest(false) {
    val teams = game.teams

    @Test
    @DisplayName("There are only four teams")
    fun thereAreOnlyFourTeams() {
        assertEquals(4, Game.Team.entries.size)
    }

    @Test
    @DisplayName("Teams empty on startup")
    fun teamsEmptyOnStartup() {
        assertEquals(0u, teams.size())
        assertEquals(0, teams.getUUIDs().size)
        assertEquals(0, teams.getPlayers().size)
    }

    @Test
    @DisplayName("Hiders empty on startup")
    fun hidersEmptyOnStartup() {
        assertEquals(0u, teams.hiderCount())
        assertEquals(0, teams.getHiders().size)
        assertEquals(0, teams.getHiderPlayers().size)
    }

    @Test
    @DisplayName("Seekers empty on startup")
    fun seekersEmptyOnStartup() {
        assertEquals(0u, teams.seekerCount())
        assertEquals(0, teams.getSeekers().size)
        assertEquals(0, teams.getSeekerPlayers().size)
    }

    @Test
    @DisplayName("Spectators empty on startup")
    fun spectatorsEmptyOnStartup() {
        assertEquals(0u, teams.spectatorCount())
        assertEquals(0, teams.getSpectators().size)
        assertEquals(0, teams.getSpectatorPlayers().size)
    }

    @Test
    @DisplayName("Unassigned empty on startup")
    fun unassignedEmptyOnStartup() {
        assertEquals(0u, teams.unassignedCount())
        assertEquals(0, teams.getUnassigned().size)
        assertEquals(0, teams.getUnassignedPlayers().size)
    }

    @Test
    @DisplayName("Teams put() add a player")
    fun teamsPutAddsAPlayer() {
        assertEquals(0u, teams.size())
        teams.put(alice.uuid, Game.Team.HIDER)
        assertEquals(1u, teams.size())
        assertEquals(Game.Team.HIDER, teams.get(alice.uuid))
    }

    @Test
    @DisplayName("Teams put() can switch a players team")
    fun teamsPutSwitchesAPlayersTeam() {
        teamsPutAddsAPlayer()
        teams.put(alice.uuid, Game.Team.SEEKER)
        assertEquals(1u, teams.size())
        assertEquals(Game.Team.SEEKER, teams.get(alice.uuid))
    }

    @Test
    @DisplayName("Teams remove() removes a player's team")
    fun teamsRemoveRemovesAPlayersTeam() {
        teamsPutAddsAPlayer()
        teams.remove(alice.uuid)
        assertEquals(0u, teams.size())
        assertEquals(null, teams.get(alice.uuid))
    }

    @Test
    @DisplayName("Teams remove() is a nop if a player isnt in a team")
    fun taemsRemoveCanBeANop() {
        assertEquals(0u, teams.size())
        assertEquals(null, teams.get(alice.uuid))
        teams.remove(alice.uuid)
        assertEquals(0u, teams.size())
        assertEquals(null, teams.get(alice.uuid))
    }

    @Test
    @DisplayName("Teams clear() removes everyone from a team")
    fun teamsClearResetsTheTeams() {
        assertEquals(0u, teams.size())
        teams.put(alice.uuid, Game.Team.HIDER)
        teams.put(bob.uuid, Game.Team.SEEKER)
        assertEquals(2u, teams.size())
        teams.clear()
        assertEquals(0u, teams.size())
        assertEquals(null, teams.get(alice.uuid))
        assertEquals(null, teams.get(bob.uuid))
    }

    @Test
    @DisplayName("Teams reset() makes every player UNASSIGNED")
    fun teamsResetMakesEveryPlayerUnassigned() {
        assertEquals(0u, teams.size())
        teams.put(alice.uuid, Game.Team.HIDER)
        teams.put(bob.uuid, Game.Team.SEEKER)
        assertEquals(2u, teams.size())
        teams.reset()
        assertEquals(2u, teams.size())
        assertEquals(Game.Team.UNASSIGNED, teams.get(alice.uuid))
        assertEquals(Game.Team.UNASSIGNED, teams.get(bob.uuid))
    }

    @Test
    @DisplayName("Hiders get functions always match")
    fun hideresGetFunctionsMatch() {
        teams.put(alice.uuid, Game.Team.HIDER)
        teams.put(bob.uuid, Game.Team.HIDER)
        teams.put(eve.uuid, Game.Team.SPECTATOR)
        assertEquals(2u, teams.hiderCount())
        assertEquals(2, teams.getHiders().size)
        assertEquals(2, teams.getHiderPlayers().size)

        val uuids = teams.getHiderPlayers().map(Player::uuid)
        assertEquals(teams.getHiders().toList(), uuids)
    }

    @Test
    @DisplayName("Seekers get functions always match")
    fun seekeresGetFunctionsMatch() {
        teams.put(alice.uuid, Game.Team.SEEKER)
        teams.put(bob.uuid, Game.Team.SEEKER)
        teams.put(eve.uuid, Game.Team.SPECTATOR)
        assertEquals(2u, teams.seekerCount())
        assertEquals(2, teams.getSeekers().size)
        assertEquals(2, teams.getSeekerPlayers().size)

        val uuids = teams.getSeekerPlayers().map(Player::uuid)
        assertEquals(teams.getSeekers().toList(), uuids)
    }

    @Test
    @DisplayName("Spectators get functions always match")
    fun spectatoresGetFunctionsMatch() {
        teams.put(alice.uuid, Game.Team.SPECTATOR)
        teams.put(bob.uuid, Game.Team.SPECTATOR)
        teams.put(eve.uuid, Game.Team.UNASSIGNED)
        assertEquals(2u, teams.spectatorCount())
        assertEquals(2, teams.getSpectators().size)
        assertEquals(2, teams.getSpectatorPlayers().size)

        val uuids = teams.getSpectatorPlayers().map(Player::uuid)
        assertEquals(teams.getSpectators().toList(), uuids)
    }

    @Test
    @DisplayName("Unassigned get functions always match")
    fun unassignedGetFunctionsMatch() {
        teams.put(alice.uuid, Game.Team.UNASSIGNED)
        teams.put(bob.uuid, Game.Team.UNASSIGNED)
        teams.put(eve.uuid, Game.Team.SPECTATOR)
        assertEquals(2u, teams.unassignedCount())
        assertEquals(2, teams.getUnassigned().size)
        assertEquals(2, teams.getUnassignedPlayers().size)

        val uuids = teams.getUnassignedPlayers().map(Player::uuid)
        assertEquals(teams.getUnassigned().toList(), uuids)
    }

    @Test
    @DisplayName("Teams get() function matches the isTeam functions")
    fun teamsGetFunctionsMatchTheIsTeamFunctions() {
        teams.put(alice.uuid, Game.Team.HIDER)
        teams.put(bob.uuid, Game.Team.SEEKER)
        teams.put(eve.uuid, Game.Team.SPECTATOR)
        assertEquals(Game.Team.HIDER, teams.get(alice.uuid))
        assertEquals(Game.Team.SEEKER, teams.get(bob.uuid))
        assertEquals(Game.Team.SPECTATOR, teams.get(eve.uuid))
        assertTrue(teams.isHider(alice.uuid))
        assertTrue(teams.isSeeker(bob.uuid))
        assertTrue(teams.isSpectator(eve.uuid))
    }

    @Test
    @DisplayName("Teams only one isTeam function may be true at once")
    fun teamsOnlyOneIsTeamFunctionMayBeTrueAtOnce() {
        teams.put(alice.uuid, Game.Team.HIDER)
        assertTrue(teams.isHider(alice.uuid))
        assertFalse(teams.isSeeker(alice.uuid))
        assertFalse(teams.isSpectator(alice.uuid))
        assertFalse(teams.isUnassigned(alice.uuid))
    }

    @Test
    @DisplayName("Teams only one isTeam function may be true at once (2)")
    fun teamsOnlyOneIsTeamFunctionMayBeTrueAtOnce2() {
        teams.put(eve.uuid, Game.Team.SPECTATOR)
        assertFalse(teams.isHider(eve.uuid))
        assertFalse(teams.isSeeker(eve.uuid))
        assertTrue(teams.isSpectator(eve.uuid))
        assertFalse(teams.isUnassigned(eve.uuid))
    }
}
