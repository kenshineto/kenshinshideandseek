package cat.freya.khs

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
}
