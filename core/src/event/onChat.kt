package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.world.Player

data class ChatEvent(val plugin: Khs, val player: Player, val msg: String) : Event()

fun onChat(event: ChatEvent) {
    val (plugin, player, msg) = event
    val game = plugin.game

    if (!game.teams.isSpectator(player.uuid)) return

    // only allow spectators to chat
    // with each other
    event.cancel()
    game.teams.getSpectatorPlayers().forEach {
        val team = plugin.locale.game.team.spectator
        val name = player.name
        it.message("$team&f <$name> $msg")
    }
}
