package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.player.Player
import cat.freya.khs.runChecks

class KhsReload : Command {
    override val label = "reload"
    override val usage = listOf<String>()
    override val description = "Reload's the plugin config"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        runChecks(plugin, player) {
            gameNotInProgress()
            lobbyEmpty()
        }

        player.message(plugin.locale.prefix.default + plugin.locale.command.reloading)
        plugin
            .reloadConfig()
            .onSuccess {
                player.message(plugin.locale.prefix.default + plugin.locale.command.reloaded)
            }
            .onFailure {
                player.message(plugin.locale.prefix.default + plugin.locale.command.errorReloading)
            }
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return listOf()
    }
}
