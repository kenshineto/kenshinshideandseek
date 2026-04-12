package cat.freya.khs.command.map

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.runChecks
import cat.freya.khs.world.Player

class KhsMapRemove : Command {
    override val label = "remove"
    override val usage = listOf("map")
    override val description = "Remove a map from the plugin"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name) = args
        runChecks(plugin, player) {
            mapExists(name)
            gameNotInProgress()
            lobbyEmpty()
        }

        plugin.maps.remove(name)
        plugin.saveConfig()

        player.message(
            plugin.locale.prefix.default +
                plugin.locale.map.deleted
                    .with(name),
        )
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "map" -> plugin.maps.keys.filter { it.startsWith(typed) }
            else -> listOf()
        }
}
