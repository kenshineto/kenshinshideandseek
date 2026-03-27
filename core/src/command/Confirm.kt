package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.REQUESTS
import cat.freya.khs.command.util.Command
import cat.freya.khs.player.Player

class KhsConfirm : Command {
    override val label = "confirm"
    override val usage = listOf<String>()
    override val description = "Confirm a request of a previously run command"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val request = REQUESTS.remove(player.uuid)

        if (request == null) {
            player.message(plugin.locale.prefix.error + plugin.locale.confirm.none)
            return
        }

        if (request.expired) {
            player.message(plugin.locale.prefix.error + plugin.locale.confirm.timedOut)
            return
        }

        request.fn()
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return listOf()
    }
}
