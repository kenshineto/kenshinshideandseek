package cat.freya.khs.command.world

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.runChecks
import cat.freya.khs.world.Player

class KhsWorldCreate : Command {
    override val label = "create"
    override val usage = listOf("name", "type", "isFlat")
    override val description = "Create a new world"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name, dimensionType, isFlat) = args
        runChecks(plugin, player) { worldDoesNotExist(name) }

        val world = plugin.shim.createWorld(name, dimensionType, isFlat == "true")
        if (world == null) {
            player.message(plugin.locale.prefix.error + plugin.locale.world.addedFailed.with(name))
            return
        }

        // update world metadata
        plugin.worldsConfig.update(name, plugin.types) {
            this.type = dimensionType
        }
        plugin.saveConfig()

        player.teleport(world.getSpawn())
        player.message(plugin.locale.prefix.default + plugin.locale.world.added.with(name))
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return when (parameter) {
            "name" -> {
                listOf(parameter)
            }

            "type" -> {
                plugin.shim.getDimensionTypes().filter { it.startsWith(typed) }
            }

            "isFlat" -> listOf("true", "false").filter { it.startsWith(typed) }

            else -> {
                listOf()
            }
        }
    }
}
