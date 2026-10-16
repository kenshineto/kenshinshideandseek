package cat.freya.khs.fabric.event

import cat.freya.khs.event.RespawnEvent
import cat.freya.khs.event.onRespawn
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.minecraft.server.level.ServerPlayer

class PlayerListener(val mod: KhsMod) {
    init {
        ServerPlayerEvents.AFTER_RESPAWN.register { _, player, _ ->
            handleRespawn(player)
        }
    }

    private fun handleRespawn(player: ServerPlayer) {
        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = RespawnEvent(mod.khs, khsPlayer)
        onRespawn(khsEvent)
    }
}
