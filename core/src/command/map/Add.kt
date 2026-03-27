package cat.freya.khs.command.map

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.config.MapConfig
import cat.freya.khs.game.KhsMap
import cat.freya.khs.player.Player
import cat.freya.khs.runChecks

class KhsMapAdd : Command {
    override val label = "add"
    override val usage = listOf("name", "world")
    override val description = "Add a map to the plugin"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name, world) = args
        runChecks(plugin, player) {
            mapDoesNotExist(name)
            mapNameValid(name)
            worldValid(world)
        }

        plugin.maps[name] = KhsMap(name, MapConfig(world), plugin)
        plugin.saveConfig()

        player.message(plugin.locale.prefix.default + plugin.locale.map.created.with(name))
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "name" -> listOf("name")
            "world" -> plugin.shim.worlds.filter { it.startsWith(typed) }
            else -> listOf()
        }
}
