package cat.freya.khs

import org.junit.jupiter.api.Test

class LifetimeTest : KhsTest() {
    @Test
    fun init() {
        plugin.init()
    }

    @Test
    fun doTick() {
        plugin.init()
        plugin.doTick()
    }
}
