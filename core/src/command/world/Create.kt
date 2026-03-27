package cat.freya.khs.command.world

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.player.Player
import cat.freya.khs.runChecks
import cat.freya.khs.world.World

class KhsWorldCreate : Command {
    override val label = "create"
    override val usage = listOf("name", "type")
    override val description = "Create a new world"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name, typeStr) = args
        runChecks(plugin, player) { worldDoesNotExist(name) }

        val type =
            World.Type.values().find { it.name.lowercase() == typeStr.lowercase() }
                ?: World.Type.NORMAL

        val world = plugin.shim.createWorld(name, type)
        if (world == null) {
            player.message(plugin.locale.prefix.error + plugin.locale.world.addedFailed.with(name))
            return
        }

        player.teleport(world.spawn.withWorld(name))
        player.message(plugin.locale.prefix.default + plugin.locale.world.added.with(name))
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return when (parameter) {
            "name" -> listOf(parameter)
            "type" ->
                World.Type.values().map { it.name.lowercase() }.filter { it.startsWith(typed) }
            else -> listOf()
        }
    }
}
