package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.world.Player

data class InteractEvent(val plugin: Khs, val player: Player, val block: String?) : Event()

fun onInteract(event: InteractEvent) {
    val (plugin, player, block) = event
    val game = plugin.game

    if (!game.teams.contains(player.uuid)) return

    // seekers are not allowed to interact
    // with anything
    if (game.teams.isSpectator(player.uuid)) {
        event.cancel()
        return
    }

    // check if the block the player may
    // be interacting with is blocked
    if (block == null) return

    if (plugin.config.blockedInteracts.any { it.equals(block, ignoreCase = true) }) {
        // this interaction is blocked!
        event.cancel()
        return
    }
}
