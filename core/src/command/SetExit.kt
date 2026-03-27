package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.player.Player
import cat.freya.khs.runChecks

class KhsSetExit : Command {
    override val label = "setexit"
    override val usage = listOf<String>()
    override val description = "Sets the plugins's exit location"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        runChecks(plugin, player) { gameNotInProgress() }

        plugin.config.exit = player.location
        plugin.saveConfig()

        player.message(plugin.locale.prefix.default + plugin.locale.map.set.exit)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return listOf()
    }
}
