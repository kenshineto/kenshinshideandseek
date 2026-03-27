package cat.freya.khs.command.map

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.player.Player

class KhsMapList : Command {
    override val label = "list"
    override val usage = listOf<String>()
    override val description = "List maps known to the plugin"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        if (plugin.maps.isEmpty()) {
            player.message(plugin.locale.prefix.default + plugin.locale.map.none)
            return
        }

        player.message(
            buildString {
                appendLine(plugin.locale.prefix.default + plugin.locale.map.list)
                for ((name, map) in plugin.maps) {
                    append("&e- &f$name: ")
                    appendLine(if (map.setup) "&aSETUP" else "&cNOT SETUP")
                }
            }
        )
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return listOf()
    }
}
