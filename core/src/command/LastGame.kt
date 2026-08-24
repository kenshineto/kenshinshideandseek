package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.world.Player

class KhsLastGame : Command {
    override val label = "lastGame"
    override val usage = listOf<String>()
    override val description = "Shows the results of the last game on this server"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val lastWinners = plugin.game.getLastWinners().keys.mapNotNull { plugin.shim.getPlayer(it) }
        val lastLoosers = plugin.game.getLastLoosers().keys.mapNotNull { plugin.shim.getPlayer(it) }

        if (lastWinners.isEmpty() || lastLoosers.isEmpty()) {
            player.message(plugin.locale.prefix.default + plugin.locale.database.noInfo)
            return
        }

        val message = buildString {
            appendLine("&f&l" + "=".repeat(30))
            appendLine(plugin.locale.database.lastGame)
            appendLine(plugin.locale.database.lastWinners.with(lastWinners.map(Player::name).joinToString(" ")))
            appendLine(plugin.locale.database.lastLoosers.with(lastLoosers.map(Player::name).joinToString(" ")))
            appendLine("&f&l" + "=".repeat(30))
        }

        player.message(message)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return listOf()
    }
}
