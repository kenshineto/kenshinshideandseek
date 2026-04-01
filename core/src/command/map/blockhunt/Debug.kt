package cat.freya.khs.command.map.blockhunt

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.inv.createBlockHuntPicker
import cat.freya.khs.player.Player
import cat.freya.khs.runChecks

class KhsMapBlockHuntDebug : Command {
    override val label = "debug"
    override val usage = listOf("map")
    override val description = "Manually open the blockhunt picker for a map"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name) = args
        runChecks(plugin, player) {
            blockHuntSupported()
            blockHuntEnabled(name)
        }

        val map = plugin.maps[name] ?: return
        val inv = createBlockHuntPicker(plugin, map) ?: return
        player.showInventory(inv)
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
