package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.world.Player

data class PlaceEvent(val plugin: Khs, val player: Player, val material: String) : Event()

fun onPlace(event: PlaceEvent) {
    val (plugin, player, _) = event
    val game = plugin.game

    if (!game.teams.contains(player.uuid)) return

    event.cancel()
}
