package cat.freya.khs.command.map.unset

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.runChecks
import cat.freya.khs.world.Player

class KhsMapUnsetBorder : Command {
    override val label = "border"
    override val usage = listOf("map")
    override val description = "Disable the world border for a map"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name) = args
        runChecks(plugin, player) {
            mapExists(name)
            gameNotInProgress()
        }

        val map = plugin.maps[name] ?: return
        val config = map.config.worldBorder
        config.enabled = false
        config.pos = null
        config.size = null
        config.delay = null
        config.move = null

        plugin.saveConfig()
        player.message(plugin.locale.prefix.default + plugin.locale.worldBorder.disable)

        map.getWorld()?.border?.reset()
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "map" -> plugin.maps.keys.filter { it.startsWith(typed) }
            else -> listOf()
        }
}
