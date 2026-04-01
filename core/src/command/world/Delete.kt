package cat.freya.khs.command.world

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.player.Player
import cat.freya.khs.runChecks
import java.io.File

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

        val loader = plugin.shim.getWorldLoader(name)

        // sanity check
        // for the love of god, make sure were removing a world, not like
        // some ones home dir ;-;
        val lock = File(loader.dir, "session.lock")
        val data = File(loader.dir, "level.dat")
        if (!lock.exists() || !data.exists()) {
            player.message(plugin.locale.prefix.error + plugin.locale.world.doesntExist.with(name))
            return
        }

        loader.unload()
        if (!loader.dir.deleteRecursively()) {
            player.message(
                plugin.locale.prefix.error + plugin.locale.world.removedFailed.with(name)
            )
            return
        }

        player.message(plugin.locale.prefix.default + plugin.locale.world.removed.with(name))
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return when (parameter) {
            "name" -> plugin.shim.worlds.filter { it.startsWith(typed) }
            else -> listOf()
        }
    }
}
