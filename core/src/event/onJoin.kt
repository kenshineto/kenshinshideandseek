package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.world.Player

data class JoinEvent(val plugin: Khs, val player: Player) : Event()

fun onJoin(event: JoinEvent) {
    val (plugin, player) = event
    val game = plugin.game

    // save name data for user
    plugin.database?.upsertName(player.uuid, player.name)

    // uhhhh
    if (game.teams.contains(player.uuid)) game.leave(player.uuid)

    // add to team cache
    game.teams.cachePut(player)

    if (plugin.config.autoJoin) {
        game.join(player.uuid)
        return
    }

    val worldName = player.getWorld()?.name ?: return

    // no need to teleport out
    if (!plugin.config.teleportStraysToExit) {
        return
    }

    if (
        (worldName == game.map?.worldName) ||
        (worldName == game.map?.gameWorldName)
    ) {
        // teleport to exit if inside game world(s)
        plugin.config.exit?.let {
            player.teleport(it)
            player.setGameMode(Player.GameMode.ADVENTURE)
        }
    }
}
