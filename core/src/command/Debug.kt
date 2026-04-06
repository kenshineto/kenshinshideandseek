package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.menu.DebugMenu
import cat.freya.khs.runChecks
import cat.freya.khs.world.Player

class KhsDebug : Command {
    override val label = "debug"
    override val usage = listOf<String>()
    override val description = "Mess with/debug the current game"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        runChecks(plugin, player) { gameMapExists() }

        val inv = DebugMenu.create(plugin) ?: return
        player.showInventory(inv)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return listOf()
    }
}
