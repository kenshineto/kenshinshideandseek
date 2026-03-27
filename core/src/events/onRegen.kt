package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.player.Player

data class RegenEvent(val plugin: Khs, val player: Player, val natural: Boolean) : Event()

fun onRegen(event: RegenEvent) {
    val (plugin, player, natural) = event
    val game = plugin.game

    if (!game.hasPlayer(player)) return

    if (!natural || plugin.config.regenHealth) return

    event.cancel()
}
