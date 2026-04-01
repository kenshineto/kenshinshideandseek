package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.player.Player

class KhsHelp : Command {
    override val label = "help"
    override val usage = listOf("*page")
    override val description = "Lists the commands you can use"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val commands = plugin.commandGroup.commandsFor(player)
        val pageSize = 4u
        val pages = (commands.size.toUInt() + pageSize - 1u) / pageSize
        val page = maxOf(minOf(args.firstOrNull().let { it?.toUIntOrNull() } ?: 0u, pages), 1u)

        player.message(
            buildString {
                appendLine(
                    "&b=================== &fHelp: Page ($page/$pages) &b==================="
                )
                for ((label, command) in commands.chunked(pageSize.toInt())[page.toInt() - 1]) {
                    val cmd = label.substring(3)
                    val usage = command.usage.joinToString(" ")
                    val description = command.description
                    appendLine("&7?&f &b/hs &f$cmd &9$usage")
                    appendLine("&7?&f  &7&o$description")
                }
                appendLine("&b=====================================================")
            }
        )
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return listOf(parameter)
    }
}
