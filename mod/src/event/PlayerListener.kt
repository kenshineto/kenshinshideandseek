package cat.freya.khs.mod.event

import cat.freya.khs.event.RespawnEvent
import cat.freya.khs.event.onRespawn
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import dev.architectury.event.events.common.PlayerEvent
import net.minecraft.server.level.ServerPlayer

class PlayerListener(val mod: KhsMod) {
    init {
        PlayerEvent.PLAYER_RESPAWN.register { player, _, _ ->
            handleRespawn(player as ServerPlayer)
        }

        // PlayerEvent.DROP_ITEM is non functional
    }

    private fun handleRespawn(player: ServerPlayer) {
        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = RespawnEvent(mod.khs, khsPlayer)
        onRespawn(khsEvent)
    }
}
