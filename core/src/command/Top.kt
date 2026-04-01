package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.player.Player

class KhsTop : Command {
    override val label = "top"
    override val usage = listOf("*page")
    override val description = "Shows the game leaderboard"

    private fun getColor(index: UInt): Char {
        return when (index) {
            0u -> 'e'
            1u -> '7'
            2u -> '6'
            else -> 'f'
        }
    }

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        var page = args.firstOrNull()?.toUIntOrNull() ?: 0u
        page = maxOf(page, 1u) - 1u

        val pageSize = 5u
        val entries = plugin.database?.getPlayers(page, pageSize)
        if (entries.isNullOrEmpty()) {
            player.message(plugin.locale.prefix.default + plugin.locale.database.noInfo)
            return
        }

        val message = buildString {
            appendLine("&f------- &lLEADERBOARD &7(Page ${page + 1u}) &f-------")
            for ((i, entry) in entries.withIndex()) {
                val wins = entry.hiderWins + entry.seekerWins
                val idx = (pageSize * page) + i.toUInt()
                val color = getColor(idx)
                val name = entry.name ?: continue
                appendLine("&$color${idx + 1u}. &c$wins &f$name")
            }
        }

        player.message(message)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return listOf(parameter)
    }
}
