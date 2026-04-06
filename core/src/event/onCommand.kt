package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.game.Game
import cat.freya.khs.world.Player

data class CommandEvent(val plugin: Khs, val player: Player, val msg: String) : Event()

fun onCommand(event: CommandEvent) {
    val (plugin, player, msg) = event
    val game = plugin.game

    if (!game.hasPlayer(player) || game.status == Game.Status.LOBBY) return

    // parse command
    val arg1 = msg.split(Regex("\\s+")).firstOrNull()?.lowercase() ?: return
    val namespacedInvoke = if (arg1.startsWith('/')) arg1.drop(1) else arg1
    val invoke = namespacedInvoke.split(":").lastOrNull() ?: return

    // check if command blocked
    val blocked =
        plugin.config.blockedCommands.any {
            val command = it.lowercase()
            // e.g. block both minecraft:msg and msg
            command == namespacedInvoke || command == invoke
        }

    if (!blocked) return

    event.cancel()
    player.message(plugin.locale.prefix.error + plugin.locale.command.notAllowedTemp)
}
