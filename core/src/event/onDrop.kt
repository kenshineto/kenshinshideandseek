package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.type.Item
import cat.freya.khs.world.Player

data class DropEvent(val plugin: Khs, val player: Player, val item: Item) : Event(plugin)

fun onDrop(event: DropEvent) {
    val (plugin, player, _) = event
    val game = plugin.game
    event.debug()

    if (!game.teams.contains(player.uuid)) return

    if (!plugin.config.dropItems) event.cancel()
}
