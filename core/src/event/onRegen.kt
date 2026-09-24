package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.world.Player

data class RegenEvent(val plugin: Khs, val player: Player, val natural: Boolean) : Event(plugin)

fun onRegen(event: RegenEvent) {
    val (plugin, player, natural) = event
    val game = plugin.game
    event.debug()

    if (!game.teams.contains(player.uuid)) return

    if (!natural || plugin.config.regenHealth) return

    event.cancel()
}
