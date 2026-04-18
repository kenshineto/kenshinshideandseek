package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.world.Player

data class LeaveEvent(val plugin: Khs, val player: Player) : Event()

fun onLeave(event: LeaveEvent) {
    val (plugin, player) = event
    val game = plugin.game

    if (game.teams.contains(player.uuid)) game.leave(player.uuid)

    // remove player from cache
    game.teams.cacheRemove(player.uuid)

    // remove player from disguiser
    plugin.entityHider.showEntity(player)
}
