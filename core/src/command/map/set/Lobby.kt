package cat.freya.khs.command.map.set

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.player.Player
import cat.freya.khs.runChecks

class KhsMapSetLobby : Command {
    override val label = "lobby"
    override val usage = listOf("map")
    override val description = "Sets the lobby spawn location for a map"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name) = args
        runChecks(plugin, player) {
            mapExists(name)
            inMapWorld(name)
            gameNotInProgress()
        }

        val map = plugin.maps[name] ?: return
        val pos = player.location.position

        runChecks(plugin, player) { spawnInRange(map, pos) }

        map.config.spawns.lobby = pos
        map.reloadConfig()
        plugin.saveConfig()
        player.message(plugin.locale.prefix.default + plugin.locale.map.set.lobby)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "map" -> plugin.maps.keys.filter { it.startsWith(typed) }
            else -> listOf()
        }
}
