package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.world.Player

data class RespawnEvent(val plugin: Khs, val player: Player) : Event(plugin)

fun onRespawn(event: RespawnEvent) {
    val (plugin, player) = event
    val game = plugin.game
    event.debug()

    game.teams.cachePut(player)
}
