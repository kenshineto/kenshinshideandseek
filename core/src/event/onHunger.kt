package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.world.Player

data class HungerEvent(val plugin: Khs, val player: Player) : Event()

fun onHunger(event: HungerEvent) {
    val (plugin, player) = event
    val game = plugin.game

    if (!game.teams.contains(player.uuid)) return

    event.cancel()
}
