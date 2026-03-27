package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.player.Player

data class InteractEvent(val plugin: Khs, val player: Player, val block: String) : Event()

fun onInteract(event: InteractEvent) {
    val (plugin, player, block) = event
    val game = plugin.game

    if (!game.hasPlayer(player)) return

    if (plugin.config.blockedInteracts.any { it.lowercase() == block.lowercase() }) {
        // this interaction is blocked!
        event.cancel()
        return
    }
}
