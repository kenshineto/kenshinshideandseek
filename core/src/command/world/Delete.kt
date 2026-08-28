package cat.freya.khs.command.world

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.runChecks
import cat.freya.khs.world.Player

class KhsWorldDelete : Command {
    override val label = "delete"
    override val usage = listOf("name")
    override val description = "Delete an existing world"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name) = args
        runChecks(plugin, player) {
            worldExists(name)
            worldNotInUse(name)
        }

        val world = plugin.shim.getWorld(name)
        world?.unload()

        val info = plugin.shim.getWorldInfo(name) ?: return
        if (!info.dir.toFile().deleteRecursively()) {
            player.message(plugin.locale.prefix.error + plugin.locale.world.removedFailed.with(name))
            return
        }

        player.message(plugin.locale.prefix.default + plugin.locale.world.removed.with(name))
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return when (parameter) {
            "name" -> plugin.shim.getWorldNames().filter { it.startsWith(typed) }
            else -> listOf()
        }
    }
}
