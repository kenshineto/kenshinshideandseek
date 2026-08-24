package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.world.Player

class KhsWins : Command {
    override val label = "wins"
    override val usage = listOf("*player")
    override val description = "Shows stats for a given player"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val name = args.getOrNull(0) ?: player.name
        val data = plugin.database?.getPlayer(name)
        if (data == null) {
            player.message(plugin.locale.prefix.default + plugin.locale.database.noInfo)
            return
        }

        val message = buildString {
            val totalWins = data.seekerWins + data.hiderWins
            val gamesPlayed = totalWins + data.seekerLosses + data.hiderLosses
            appendLine("&f&l" + "=".repeat(30))
            appendLine(plugin.locale.database.infoFor.with(name))
            appendLine(plugin.locale.database.totalWins.with(totalWins))
            appendLine(plugin.locale.database.hiderWins.with(data.hiderWins))
            appendLine(plugin.locale.database.seekerWins.with(data.seekerWins))
            appendLine(plugin.locale.database.gamesPlayed.with(gamesPlayed))
            append("&f&l" + "=".repeat(30))
        }

        player.message(message)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return when (parameter) {
            "*player" -> plugin.database?.getPlayerNames(10u, typed) ?: listOf()
            else -> listOf()
        }
    }
}
