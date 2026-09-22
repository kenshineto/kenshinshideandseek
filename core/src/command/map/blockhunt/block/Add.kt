package cat.freya.khs.command.map.blockhunt.block

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.runChecks
import cat.freya.khs.world.Player

class KhsMapBlockHuntBlockAdd : Command {
    override val label = "add"
    override val usage = listOf("map", "block")
    override val description = "Add a block to a block hunt map"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name, blockName) = args
        runChecks(plugin, player) {
            blockHuntSupported()
            blockHuntEnabled(name)
            gameNotInProgress()
            lobbyEmpty()
        }

        val blockType = plugin.parseBlock(blockName)
        if (blockType == null) {
            player.message(plugin.locale.prefix.error + plugin.locale.blockHunt.block.unknown)
            return
        }

        val map = plugin.maps[name] ?: return
        for (block in map.config.blockHunt.blocks) {
            val inUseBlockType = plugin.parseBlock(block) ?: continue
            if (inUseBlockType == blockType) {
                player.message(plugin.locale.prefix.error + plugin.locale.blockHunt.block.exists.with(block))
                return
            }
        }

        map.config.blockHunt.blocks += blockName
        map.reloadConfig()

        plugin.saveConfig()
        player.message(plugin.locale.prefix.default + plugin.locale.blockHunt.block.added.with(blockName))
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "map" -> {
                plugin.maps.filter { it.value.config.blockHunt.enabled }.map { it.key }.filter { it.startsWith(typed) }
            }

            "block" -> {
                plugin.shim.getBlocks().filter { it.startsWith(typed) }
            }

            else -> {
                listOf()
            }
        }
}
