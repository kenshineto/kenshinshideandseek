package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.game.Game
import cat.freya.khs.player.Player
import cat.freya.khs.runChecks

class KhsStop : Command {
    override val label = "stop"
    override val usage = listOf<String>()
    override val description = "Stops the game"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        runChecks(plugin, player) {
            gameMapExists()
            gameInProgress()
        }

        plugin.game.broadcast(plugin.locale.prefix.abort + plugin.locale.game.stop)
        plugin.game.stop(Game.WinType.NONE)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return listOf()
    }
}
