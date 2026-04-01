package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.player.Player
import cat.freya.khs.world.Position

data class MoveEvent(val plugin: Khs, val player: Player, val from: Position, val to: Position) :
    Event()

private fun updateDisguise(event: MoveEvent) {
    val (plugin, player, from, to) = event
    val disguise = plugin.disguiser.getDisguise(player.uuid) ?: return

    // check if disguise should be (un)solidified (since player moved)
    if (from.distance(to) <= 0.1) {
        disguise.startSolidifying(player.location.position.clone())
    } else {
        disguise.shouldBeSolid = false
    }
}

private fun checkMapBounds(event: MoveEvent) {
    val (plugin, player, _, to) = event
    val game = plugin.game

    if (!game.hasPlayer(player)) return

    val map = game.map ?: return
    if (player.location.worldName != map.gameWorldName) return

    // check if player went out of bounds
    val canLeaveBounds = player.hasPermission("hs.leavebounds")
    if (!canLeaveBounds && map.bounds()?.inBounds(to) == false) event.cancel()
}

fun onMove(event: MoveEvent) {
    updateDisguise(event)
    checkMapBounds(event)
}
