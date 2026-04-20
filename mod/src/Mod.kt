package cat.freya.khs.mod

import cat.freya.khs.Khs
import cat.freya.khs.mod.event.*
import java.util.concurrent.atomic.AtomicBoolean

class KhsMod(val info: Info) {
    data class Info(
        val pluginVersion: String,
        val platform: String,
    )

    val enabled = AtomicBoolean(true)
    val server = ModServer(this)
    val shim = ModKhsShim(this)
    val khs = Khs(shim)

    init {
        server.init()
    }

    // called once the minecraft
    // server is starting
    fun init() {
        khs.init()

        // khs.init() can disable us
        if (!enabled.get()) return

        // TODO: register bungeecord

        registerListeners()
    }

    fun onShutdown() {
        if (!enabled.getAndSet(false)) return
        khs.cleanup()
    }

    fun onTick() {
        if (!enabled.get()) return
        khs.onTick()
    }

    private fun registerListeners() {
        BreakListener(this)
        ChatListener(this)
        // TODO: CommandListener(this)
        DamageListener(this)
        InteractListener(this)
        InventoryListener(this)
        JoinLeaveListener(this)
        MovementListener(this)
        PlayerListener(this)
        RespawnListener(this)
    }

    companion object {
        const val ID: String = "khs"
    }
}
