package cat.freya.khs

import cat.freya.khs.config.DatabaseType
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class PluginTest : KhsTest(false) {
    @Test
    @DisplayName("Plugin can initalize")
    fun pluginInit() {
        plugin.init()
    }

    @Test
    @DisplayName("Plugin can reload")
    fun pluginReload() {
        plugin.init()
        plugin.reloadConfig()
    }

    @Test
    @DisplayName("Plugin can tick")
    fun pluginDoTick() {
        plugin.init()
        plugin.doTick()
    }

    @Test
    @DisplayName("Plugin can cleanup")
    fun pluginCleanup() {
        plugin.init()
        setupMap()
        game.join(alice.uuid)
        game.join(bob.uuid)
        assertEquals(2u, game.teams.size())
        plugin.cleanup()
        assertEquals(0u, game.teams.size())
        assertEquals(config.exit, alice.getLocation())
        assertEquals(config.exit, bob.getLocation())
    }

    @Test
    @DisplayName("Plugin crashes if it cannot connect to the database")
    fun pluginCrashesOnDatabaseFailure() {
        // we will have no postgres serer running, mwahahaha
        config.database.type = DatabaseType.POSTGRES
        config.database.port = 0u // lol
        assertThrows { plugin.init() }
    }
}
