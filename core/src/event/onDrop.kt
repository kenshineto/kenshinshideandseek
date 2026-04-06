package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.world.Item
import cat.freya.khs.world.Player

data class DropEvent(val plugin: Khs, val player: Player, val item: Item) : Event()

fun onDrop(event: DropEvent) {
    val (plugin, player, _) = event
    val game = plugin.game

    if (!game.hasPlayer(player)) return

    if (!plugin.config.dropItems) event.cancel()
}
