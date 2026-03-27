package cat.freya.khs.command.map

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.player.Player
import cat.freya.khs.runChecks

class KhsMapGoTo : Command {
    override val label = "goto"
    override val usage = listOf("map", "spawn")
    override val description = "Goes to a spawn location for a map"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name, spawn) = args
        runChecks(plugin, player) { mapExists(name) }

        var map = plugin.maps.get(name) ?: return
        val loc =
            when (spawn) {
                "spawn" -> map.gameSpawn
                "lobby" -> map.lobbySpawn
                "seekerlobby" -> map.seekerLobbySpawn
                else -> null
            }

        if (loc == null) {
            player.message(plugin.locale.prefix.error + plugin.locale.map.error.locationNotSet)
            return
        }

        loc.teleport(player)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "map" -> plugin.maps.keys.filter { it.startsWith(typed) }
            "spawn" -> listOf("spawn", "lobby", "seekerlobby").filter { it.startsWith(typed) }
            else -> listOf()
        }
}
