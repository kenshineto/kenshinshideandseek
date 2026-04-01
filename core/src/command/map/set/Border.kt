package cat.freya.khs.command.map.set

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.player.Player
import cat.freya.khs.runChecks

class KhsMapSetBorder : Command {
    override val label = "border"
    override val usage = listOf("map", "size", "delay", "move")
    override val description = "Enable the world border for a map"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name, sizeS, delayS, moveS) = args
        runChecks(plugin, player) {
            mapExists(name)
            inMapWorld(name)
            gameNotInProgress()
        }

        val size = sizeS.toULong()
        val delay = delayS.toULong()
        val move = moveS.toULong()

        if (size < 100u) {
            player.message(plugin.locale.prefix.error + plugin.locale.worldBorder.minSize)
            return
        }

        if (move < 1u) {
            player.message(plugin.locale.prefix.error + plugin.locale.worldBorder.minChange)
            return
        }

        val map = plugin.maps[name] ?: return
        val config = map.config.worldBorder
        config.enabled = true
        config.pos = player.location.position
        config.size = size
        config.delay = delay
        config.move = move

        runChecks(plugin, player) {
            // note this is not error, only warn
            spawnsInRange(map)
        }

        map.reloadConfig()

        plugin.saveConfig()
        player.message(
            plugin.locale.prefix.default + plugin.locale.worldBorder.enable.with(size, delay, move)
        )

        val loc = player.location.position
        map.world?.border?.move(loc.x, loc.z, size, 0UL)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "map" -> plugin.maps.keys.filter { it.startsWith(typed) }
            else -> listOf(parameter)
        }
}
