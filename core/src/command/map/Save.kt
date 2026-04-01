package cat.freya.khs.command.map

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.game.mapSave
import cat.freya.khs.player.Player
import cat.freya.khs.runChecks

class KhsMapSave : Command {
    override val label = "save"
    override val usage = listOf("map")
    override val description = "Save the map backup used for gameplay"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name) = args
        runChecks(plugin, player) {
            mapExists(name)
            gameNotInProgress()
            lobbyEmpty()
        }

        val map = plugin.maps[name] ?: return
        mapSave(plugin, map)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "map" -> plugin.maps.keys.filter { it.startsWith(typed) }
            else -> listOf()
        }
}
