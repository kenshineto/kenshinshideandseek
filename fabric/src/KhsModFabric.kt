package cat.freya.khs.fabric

import cat.freya.khs.mod.KhsMod
import net.fabricmc.api.ModInitializer

object KhsModFabric : ModInitializer {
    override fun onInitialize() {
        KhsMod("Fabric")
    }
}
