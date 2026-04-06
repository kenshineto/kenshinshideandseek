package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.runChecks
import cat.freya.khs.world.Player

class KhsLeave : Command {
    override val label = "leave"
    override val usage = listOf<String>()
    override val description = "Leaves the game lobby"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        runChecks(plugin, player) { playerInGame() }

        plugin.game.leave(player.uuid)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return listOf()
    }
}
