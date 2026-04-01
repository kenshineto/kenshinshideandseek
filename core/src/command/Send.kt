package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.player.Player
import cat.freya.khs.runChecks

class KhsSend : Command {
    override val label = "send"
    override val usage = listOf("map")
    override val description = "Send the current lobby to another map"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val map = plugin.maps[args.first()]

        runChecks(plugin, player) {
            gameNotInProgress()
            playerInGame()
            mapSetup(map)
        }

        plugin.game.setMap(map)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "map" -> plugin.maps.keys.filter { it.startsWith(typed) }
            else -> listOf()
        }
}
