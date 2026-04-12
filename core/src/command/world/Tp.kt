package cat.freya.khs.command.world

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.runChecks
import cat.freya.khs.world.Player

class KhsWorldTp : Command {
    override val label = "tp"
    override val usage = listOf("name")
    override val description = "Teleport to a world's spawn"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name) = args
        runChecks(plugin, player) { worldExists(name) }

        val loader = plugin.shim.getWorldLoader(name)
        val world = loader.load()
        if (world == null) {
            player.message(
                plugin.locale.prefix.error +
                    plugin.locale.world.loadFailed
                        .with(name),
            )
            return
        }

        val spawn = world.getSpawn()
        player.teleport(spawn)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return when (parameter) {
            "name" -> plugin.shim.getWorldNames().filter { it.startsWith(typed) }
            else -> listOf()
        }
    }
}
