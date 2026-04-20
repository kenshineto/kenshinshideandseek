package cat.freya.khs.fabric

import cat.freya.khs.mod.KhsMod
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader

object KhsModFabric : ModInitializer {
    override fun onInitialize() {
        val loader = FabricLoader.getInstance()
        val container = loader.getModContainer(KhsMod.ID).get()

        val pluginVersion = container.metadata?.version?.friendlyString ?: "null"
        val info = KhsMod.Info(pluginVersion, "Fabric")

        KhsMod(info)
    }
}
