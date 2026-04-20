package cat.freya.khs.mod.event

import cat.freya.khs.event.JoinEvent
import cat.freya.khs.event.LeaveEvent
import cat.freya.khs.event.onJoin
import cat.freya.khs.event.onLeave
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import dev.architectury.event.events.common.PlayerEvent
import net.minecraft.server.level.ServerPlayer

class JoinLeaveListener(val mod: KhsMod) {
    init {
        PlayerEvent.PLAYER_JOIN.register { player ->
            handleJoin(player)
        }

        PlayerEvent.PLAYER_QUIT.register { player ->
            handleLeave(player)
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
