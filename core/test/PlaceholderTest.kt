package cat.freya.khs

import cat.freya.khs.game.Game
import java.util.UUID
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class PlaceholderTest : KhsTest() {
    val invalid = locale.placeholder.invalid
    val noData = locale.placeholder.noData

    fun mkPlaceholder(placeholder: String, player: UUID? = null): PlaceholderRequest {
        val uuid = player ?: UUID(0L, 0L)
        return PlaceholderRequest(plugin, uuid, placeholder)
    }

    fun assertPlaceholder(placeholder: String, expected: String, player: UUID? = null) {
        val result = handlePlaceholder(mkPlaceholder(placeholder, player))
        assertEquals(expected, result, "placeholder '${placeholder}' failed!")
    }

    @Test
    @DisplayName("Team counts are zero on startup")
    fun teamCountsAreZeroOnStartup() {
        assertPlaceholder("hiders", "0")
        assertPlaceholder("seekers", "0")
        assertPlaceholder("spectators", "0")
    }

    @Test
    @DisplayName("Team counts are correct during gameplay")
    fun teamCountsAreCorrectDuringGameplay() {
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        teamCountsAreZeroOnStartup()
        game.start()
        assertPlaceholder("hiders", "1")
        assertPlaceholder("seekers", "1")
        assertPlaceholder("spectators", "0")
        game.join(eve.uuid)
        assertPlaceholder("spectators", "1")
    }

    @Test
    @DisplayName("hs_map returns map name")
    fun mapReturnsMapName() {
        assertPlaceholder("map", noData)
        val map = setupMap()
        assertPlaceholder("map", noData)
        game.setMap(map)
        assertPlaceholder("map", map.name)
    }

    @Test
    @DisplayName("hs_last is functional")
    fun lastIsFunctional() {
        assertPlaceholder("last", invalid)
        assertPlaceholder("last_win", noData)
        assertPlaceholder("last_loose", noData)
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        game.start(setOf(alice.uuid))
        assertStatus(Game.Status.HIDING)
        assertEquals(2u, game.teams.size())
        assertEquals(2, game.teams.getUUIDs().size)
        assertPlaceholder("last_win", noData)
        assertPlaceholder("last_loose", noData)
        game.stop(Game.WinType.SEEKERS_WIN)
        assertEquals(2, game.teams.getUUIDs().size)
        assertPlaceholder("last_win", alice.name)
        assertPlaceholder("last_loose", bob.name)
        assertPlaceholder("last_win_0", alice.name)
        assertPlaceholder("last_loose_0", bob.name)
        assertPlaceholder("last_win_1", noData)
        assertPlaceholder("last_loose_1", noData)
        assertPlaceholder("last_win_seeker", alice.name)
        assertPlaceholder("last_loose_hider", bob.name)
        assertPlaceholder("last_win_seeker_0", alice.name)
        assertPlaceholder("last_loose_hider_0", bob.name)
        assertPlaceholder("last_win_seeker_1", noData)
        assertPlaceholder("last_loose_hider_1", noData)
    }
}
