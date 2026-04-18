package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.world.Player
import cat.freya.khs.world.Position

data class MoveEvent(val plugin: Khs, val player: Player, val from: Position, val to: Position) : Event()

fun onMove(event: MoveEvent) {
    val (plugin, player, _, to) = event
    val game = plugin.game

    if (!game.teams.contains(player.uuid)) return

    val map = game.map ?: return
    if (player.getLocation().worldName != map.gameWorldName) return

    // check if player went out of bounds
    val canLeaveBounds = player.hasPermission("hs.leavebounds")
    if (!canLeaveBounds && map.getBounds()?.inBounds(to) == false) {
        event.cancel()
    }
}
