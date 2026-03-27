package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.inv.createDebugMenu
import cat.freya.khs.player.Player
import cat.freya.khs.runChecks

class KhsDebug : Command {
    override val label = "debug"
    override val usage = listOf<String>()
    override val description = "Mess with/debug the current game"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        runChecks(plugin, player) { gameMapExists() }

        val inv = createDebugMenu(plugin) ?: return
        player.showInventory(inv)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return listOf()
    }
}
