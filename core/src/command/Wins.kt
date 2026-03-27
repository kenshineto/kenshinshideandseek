package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.player.Player

class KhsWins : Command {
    override val label = "wins"
    override val usage = listOf("player")
    override val description = "Shows stats for a given player"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name) = args
        val data = plugin.database?.getPlayer(name)
        if (data == null) {
            player.message(plugin.locale.prefix.default + plugin.locale.database.noInfo)
            return
        }

        val message = buildString {
            val wins = data.seekerWins + data.hiderWins
            val games = wins + data.seekerLosses + data.hiderLosses
            appendLine("&f&l" + "=".repeat(30))
            appendLine(plugin.locale.database.infoFor.with(name))
            appendLine("&bTOTAL WINS: &f$wins")
            appendLine("&6HIDER WINS: &f${data.hiderWins}")
            appendLine("&cSEEKER WINS: &f${data.seekerWins}")
            appendLine("GAMES PLAYED: ${games}")
            append("&f&l" + "=".repeat(30))
        }

        player.message(message)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return when (parameter) {
            "player" -> plugin.database?.getPlayerNames(10u, typed) ?: listOf()
            else -> listOf()
        }
    }
}
