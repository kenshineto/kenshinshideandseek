package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.game.Game
import cat.freya.khs.player.Player

data class CommandEvent(val plugin: Khs, val player: Player, val msg: String) : Event()

fun onCommand(event: CommandEvent) {
    val (plugin, player, msg) = event
    val game = plugin.game

    if (!game.hasPlayer(player) || game.status == Game.Status.LOBBY) return

    val invoke = msg.split(" ").firstOrNull()?.lowercase() ?: return
    if (!plugin.config.blockedCommands.any { it.lowercase() == invoke }) return

    event.cancel()
    player.message(plugin.locale.prefix.error + plugin.locale.command.notAllowedTemp)
}
