package cat.freya.khs.fabric

import cat.freya.khs.Khs
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import java.util.concurrent.atomic.AtomicBoolean

object KhsMod : ModInitializer {
    const val ID: String = "khs"
    private val enabled: AtomicBoolean = AtomicBoolean(true)

    lateinit var loader: FabricLoader
    lateinit var container: ModContainer
    lateinit var server: FabricServer
    lateinit var shim: FabricKhsShim
    lateinit var khs: Khs

    override fun onInitialize() {
        loader = FabricLoader.getInstance()
        container = loader.getModContainer(ID).get()

        server = FabricServer(this)
        shim = FabricKhsShim(this)
        khs = Khs(shim)

        khs.init()

        // khs.init() can disable us
        if (!enabled.get()) return

        server.init()

        // TODO: register bungeecord

        // TODO: register event listeners
    }

    fun onShutdown() {
        if (!enabled.getAndSet(false)) return
        khs.cleanup()
    }

    fun onTick() {
        if (!enabled.get()) return
        khs.onTick()
    }
}
