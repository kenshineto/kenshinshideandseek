package cat.freya.khs.fabric

import cat.freya.khs.Khs
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.optionals.getOrNull
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer

object KhsMod : ModInitializer {
    val server: FabricKhsServer = FabricKhsServer(this)
    val shim: FabricKhsShim = FabricKhsShim(this)
    val khs: Khs = Khs(shim)

    const val ID: String = "KenshinsHideAndSeek"
    private var enabled: AtomicBoolean = AtomicBoolean(true)

    val loader: FabricLoader
        get() = FabricLoader.getInstance() ?: error("could not get fabric loader")

    val container: ModContainer
        get() = loader.getModContainer(ID).getOrNull() ?: error("could not get mod container")

    override fun onInitialize() {
        server.init {
            // server has been registered
            // it is now safe to call into :core
            khs.init()

            // khs.init() can disable us
            if (!enabled.get()) return@init

            // TODO: register bungeecord

            // TODO: register event listeners
        }
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
