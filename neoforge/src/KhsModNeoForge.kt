package cat.freya.khs.neoforge

import cat.freya.khs.mod.KhsMod
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(KhsMod.ID)
class KhsModNeoForce(val container: ModContainer) {
    init {
        val pluginVersion = container.modInfo.version.toString()
        val info = KhsMod.Info(pluginVersion, "NeoForge")
        KhsMod(info)
    }
}
