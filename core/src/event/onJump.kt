package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.world.Player

data class JumpEvent(val plugin: Khs, val player: Player) : Event()

fun onJump(event: JumpEvent) {
    val (plugin, player) = event
    val game = plugin.game

    if (!game.teams.isSpectator(player.uuid)) return

    if (player.getAllowedFlight()) {
        player.setFlying(true)
    }
}
