package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.player.Player

data class KickEvent(val plugin: Khs, val player: Player, val reason: String) : Event()

fun onKick(event: KickEvent) {
    val (plugin, player, reason) = event

    // spectators are allowed to fly
    // this also can be triggered by blockhunt
    if (reason.lowercase().contains("flying")) {
        event.cancel()
        return
    }

    // handle leave
    onLeave(LeaveEvent(plugin, player))
}
