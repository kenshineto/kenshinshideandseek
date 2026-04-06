package cat.freya.khs.command.map.set

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.runChecks
import cat.freya.khs.world.Player

class KhsMapSetSpawn : Command {
    override val label = "spawn"
    override val usage = listOf("map")
    override val description = "Sets the game spawn location for a map"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name) = args
        runChecks(plugin, player) {
            mapExists(name)
            inMapWorld(name)
            gameNotInProgress()
        }

        val map = plugin.maps[name] ?: return
        val pos = player.getLocation().toPosition()

        runChecks(plugin, player) { spawnInRange(map, pos) }

        map.config.spawns.game = pos.toLegacy()
        map.reloadConfig()

        plugin.saveConfig()
        player.message(plugin.locale.prefix.default + plugin.locale.map.set.gameSpawn)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "map" -> plugin.maps.keys.filter { it.startsWith(typed) }
            else -> listOf()
        }
}
