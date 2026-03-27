package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.player.Player
import cat.freya.khs.world.Position

data class MoveEvent(val plugin: Khs, val player: Player, val to: Position) : Event()

fun onMove(event: MoveEvent) {
    val (plugin, player, to) = event
    val game = plugin.game

    if (!game.hasPlayer(player)) return

    val map = game.map ?: return
    if (player.location.worldName != map.gameWorldName) return

    if (player.hasPermission("hs.leavebounds")) return

    if (map.bounds()?.inBounds(to) == false) event.cancel()
}
