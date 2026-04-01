package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.player.Player
import cat.freya.khs.runChecks

class KhsJoin : Command {
    override val label = "join"
    override val usage = listOf("*map")
    override val description = "Joins the game, and can set a map if the lobby is empty"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val mapName = args.firstOrNull()
        val map = mapName?.let { plugin.maps[it] }

        runChecks(plugin, player) {
            gameMapExists()
            playerNotInGame()
            if (mapName != null) mapSetup(map)
        }

        if (plugin.game.size == 0u) plugin.game.setMap(map)

        plugin.game.join(player.uuid)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "*map" -> plugin.maps.keys.filter { it.startsWith(typed) }
            else -> listOf()
        }
}
