package cat.freya.khs.fabric

import cat.freya.khs.Khs
import java.io.File
import kotlin.jvm.optionals.getOrNull
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer

object KhsMod : ModInitializer {
    val shim: FabricKhsShim = FabricKhsShim(this)
    val khs: Khs = Khs(shim)

    const val id: String = "KenshinsHideAndSeek"
    var loader: FabricLoader? = null
    var container: ModContainer? = null
    @Volatile var enabled: Boolean = true

    override fun onInitialize() {
        loader = FabricLoader.getInstance() ?: error("failed to get fabric loader")
        container = loader?.getModContainer(id)?.getOrNull() ?: error("failed to get mod container")

        ServerLifecycleEvents.SERVER_STOPPING.register { _ -> onShutdown() }
    }

    fun onShutdown() {
        if (!enabled) return
        enabled = false

        // cleanup handlers and stuff
        error("todo")
    }

    // return the mod config dir
    fun configDir(): File = loader!!.configDir.resolve(id).toFile()
}
