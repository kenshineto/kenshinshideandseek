package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.world.Player

class KhsLastGame : Command {
    override val label = "lastGame"
    override val usage = listOf<String>()
    override val description = "Shows the winners of the last game in this server"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val lastWinners = plugin.game.getLastWinners().mapNotNull { plugin.shim.getPlayer(it) }

        if (lastWinners.isEmpty()) {
            player.message(plugin.locale.prefix.default + plugin.locale.database.noInfo)
            return
        }

        val message = buildString {
            appendLine(plugin.locale.database.lastWinners)
            for (player in lastWinners) {
                appendLine("- ${player.name}")
            }
        }

        player.message(message)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return listOf()
    }
}
