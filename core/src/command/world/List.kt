package cat.freya.khs.command.world

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.world.Player
import cat.freya.khs.world.World

class KhsWorldList : Command {
    override val label = "list"
    override val usage = listOf<String>()
    override val description = "Teleport to a world's spawn"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val worlds = plugin.shim.getWorldNames()
        if (worlds.isEmpty()) {
            // uhhh, we have to be in a world to call this 0_0
            player.message(plugin.locale.prefix.error + plugin.locale.world.none)
            return
        }

        val message =
            buildString {
                appendLine(plugin.locale.world.list)
                for (worldName in worlds) {
                    val world = plugin.shim.getWorld(worldName)
                    val status =
                        when (world?.type) {
                            World.Type.NORMAL -> "&aNORMAL"
                            World.Type.FLAT -> "&aFLAT"
                            World.Type.NETHER -> "&cNETHER"
                            World.Type.END -> "&eEND"
                            else -> "&7NOT LOADED"
                        }
                    appendLine("&e- &f$worldName: $status")
                }
            }
        player.message(message)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return listOf()
    }
}
