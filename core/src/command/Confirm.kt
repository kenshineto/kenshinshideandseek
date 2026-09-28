package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.world.Player

class KhsConfirm : Command {
    override val label = "confirm"
    override val usage = listOf<String>()
    override val description = "Confirm a request of a previously run command"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        plugin.requestManager.execute(player)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return listOf()
    }
}
