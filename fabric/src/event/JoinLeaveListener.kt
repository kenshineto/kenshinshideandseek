package cat.freya.khs.fabric.event

import cat.freya.khs.event.JoinEvent
import cat.freya.khs.event.LeaveEvent
import cat.freya.khs.event.onJoin
import cat.freya.khs.event.onLeave
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.level.ServerPlayer

class JoinLeaveListener(val mod: KhsMod) {
    init {
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            handleJoin(handler.player)
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            handleLeave(handler.player)
        }
    }

    private fun handleJoin(player: ServerPlayer) {
        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = JoinEvent(mod.khs, khsPlayer)
        onJoin(khsEvent)
    }

    private fun handleLeave(player: ServerPlayer) {
        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = LeaveEvent(mod.khs, khsPlayer)
        onLeave(khsEvent)
    }
}
