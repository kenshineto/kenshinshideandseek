package cat.freya.khs.command.map.blockhunt.block

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.runChecks
import cat.freya.khs.world.Player

class KhsMapBlockHuntBlockList : Command {
    override val label = "list"
    override val usage = listOf("map")
    override val description = "List blocks in use on a block hunt map"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name) = args
        runChecks(plugin, player) {
            blockHuntSupported()
            blockHuntEnabled(name)
        }

        val map = plugin.maps[name] ?: return
        val blocks = map.config.blockHunt.blocks
        if (blocks.isEmpty()) {
            player.message(plugin.locale.prefix.default + plugin.locale.blockHunt.block.none)
            return
        }

        val message = buildString {
            appendLine(plugin.locale.blockHunt.block.list)
            for (block in blocks) {
                appendLine("&e- &f$block")
            }
        }

        player.message(message)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "map" ->
                plugin.maps
                    .filter { it.value.config.blockHunt.enabled }
                    .map { it.key }
                    .filter { it.startsWith(typed) }
            else -> listOf()
        }
}
