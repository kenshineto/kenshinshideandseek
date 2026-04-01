package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.player.Player

data class JoinEvent(val plugin: Khs, val player: Player) : Event()

fun onJoin(event: JoinEvent) {
    val (plugin, player) = event
    val game = plugin.game

    // save name data for user
    plugin.database?.upsertName(player.uuid, player.name)

    // uhhhh
    if (game.hasPlayer(player)) game.leave(player.uuid)

    if (plugin.config.autoJoin) {
        game.join(player.uuid)
        return
    }

    val worldName = player.world?.name ?: return
    if (
        (plugin.config.teleportStraysToExit && worldName == game.map?.worldName) ||
            ((plugin.config.teleportStraysToExit || plugin.config.mapSaveEnabled) &&
                worldName == game.map?.gameWorldName)
    ) {
        // teleport to exit if inside game world(s)
        plugin.config.exit?.let {
            player.teleport(it)
            player.gameMode = Player.GameMode.ADVENTURE
        }
    }
}
