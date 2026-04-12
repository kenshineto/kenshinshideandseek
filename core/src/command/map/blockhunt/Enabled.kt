package cat.freya.khs.command.map.blockhunt

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.runChecks
import cat.freya.khs.world.Player

class KhsMapBlockHuntEnabled : Command {
    override val label = "enabled"
    override val usage = listOf("map", "bool")
    override val description = "Enable/disable blockhunt on a map"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name, enabled) = args
        runChecks(plugin, player) {
            blockHuntSupported()
            mapExists(name)
            gameNotInProgress()
        }

        val map = plugin.maps[name] ?: return
        map.config.blockHunt.enabled = (enabled.lowercase() == "true")
        map.reloadConfig()

        val msg =
            if (map.config.blockHunt.enabled) {
                plugin.locale.blockHunt.enabled
            } else {
                plugin.locale.blockHunt.disabled
            }

        plugin.saveConfig()
        player.message(plugin.locale.prefix.default + msg)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "map" -> plugin.maps.keys.filter { it.startsWith(typed) }
            "bool" -> listOf("true", "false").filter { it.startsWith(typed) }
            else -> listOf()
        }
}
