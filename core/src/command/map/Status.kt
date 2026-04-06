package cat.freya.khs.command.map

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.runChecks
import cat.freya.khs.world.Player

class KhsMapStatus : Command {
    override val label = "status"
    override val usage = listOf("map")
    override val description = "Says what is needed to fully setup the map"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        val (name) = args
        runChecks(plugin, player) { mapExists(name) }

        val map = plugin.maps[name] ?: return

        if (map.isSetup()) {
            player.message(plugin.locale.prefix.default + plugin.locale.map.setup.complete)
            return
        }

        player.message(
            buildString {
                appendLine(plugin.locale.map.setup.header)
                if (map.gameSpawn == null) appendLine(plugin.locale.map.setup.game)
                if (map.lobbySpawn == null) appendLine(plugin.locale.map.setup.lobby)
                if (map.seekerLobbySpawn == null) appendLine(plugin.locale.map.setup.seekerLobby)
                if (plugin.config.exit == null) appendLine(plugin.locale.map.setup.exit)
                if (map.getBounds() == null) appendLine(plugin.locale.map.setup.bounds)
                if (plugin.config.mapSaveEnabled && !map.hasMapSave())
                    appendLine(plugin.locale.map.setup.saveMap)
                if (map.config.blockHunt.enabled && map.config.blockHunt.blocks.isEmpty())
                    appendLine(plugin.locale.map.setup.blockHunt)
            }
        )
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> =
        when (parameter) {
            "map" -> plugin.maps.keys.filter { it.startsWith(typed) }
            else -> listOf()
        }
}
