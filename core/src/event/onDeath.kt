package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.world.Player

data class DeathEvent(val plugin: Khs, val player: Player) : Event()

fun onDeath(event: DeathEvent) {
    val (plugin, player) = event
    val game = plugin.game

    // uh, if u dead, kinda aren't disguised anymore lol
    plugin.disguiser.reveal(player.uuid)

    if (!game.teams.contains(player.uuid)) return

    event.cancel()
}
