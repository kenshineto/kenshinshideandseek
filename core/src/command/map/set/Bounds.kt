package cat.freya.khs.command.map.set

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.config.BoundConfig
import cat.freya.khs.runChecks
import cat.freya.khs.world.Player

class KhsMapSetBounds : Command {
    override val label = "bounds"
    override val usage = listOf("map")
    override val description = "Sets the map bounds for a map"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name) = args
        runChecks(plugin, player) {
            mapExists(name)
            inMapWorld(name)
            gameNotInProgress()
        }

        val map = plugin.maps[name] ?: return
        val config = map.config.bounds

        val pos = player.getLocation().toPosition()
        val num: Int

        if (config.min == null || config.max != null) {
            config.min = BoundConfig(pos.x, pos.z)
            config.max = null
            num = 1
        } else {
            val minX = minOf(config.min?.x ?: 0.0, pos.x)
            val minZ = minOf(config.min?.z ?: 0.0, pos.z)
            val maxX = maxOf(config.min?.x ?: 0.0, pos.x)
            val maxZ = maxOf(config.min?.z ?: 0.0, pos.z)
            config.min = BoundConfig(minX, minZ)
            config.max = BoundConfig(maxX, maxZ)
            num = 2
        }

        runChecks(plugin, player) {
            // note this is not error, only warn
            spawnsInRange(map)
        }

        map.reloadConfig()
        plugin.saveConfig()
        player.message(plugin.locale.prefix.default + plugin.locale.map.set.bounds.with(num))
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "map" -> plugin.maps.keys.filter { it.startsWith(typed) }
            else -> listOf()
        }
}
