package cat.freya.khs.command.map.blockhunt

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.runChecks
import cat.freya.khs.world.Player

class KhsMapBlockHuntDisguise : Command {
    override val label = "disguise"
    override val usage = listOf("block")
    override val description = "Disguise oneself as any block"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (blockName) = args
        runChecks(plugin, player) { blockHuntSupported() }

        val material = plugin.parseMaterial(blockName)
        if (material == null) {
            player.message(plugin.locale.prefix.error + plugin.locale.blockHunt.block.unknown)
            return
        }

        plugin.disguiser.disguise(player, material)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "block" -> {
                plugin.shim
                    .getBlocks()
                    .map { it.key.platformKey }
                    .filter { it.startsWith(typed) }
            }

            else -> {
                listOf()
            }
        }
}
