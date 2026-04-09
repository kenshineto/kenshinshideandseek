package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.world.Player

data class InteractEvent(
    val plugin: Khs,
    val player: Player,
    val action: Action,
    val block: String?,
) : Event() {
    enum class Action {
        LEFT_CLICK,
        RIGHT_CLICK,
        ATTACK,
    }
}

fun onInteract(event: InteractEvent) {
    val (plugin, player, _, block) = event
    val game = plugin.game

    if (!game.hasPlayer(player)) return

    // seekers are not allowed to interact
    // with anything
    if (game.isSpectator(player)) {
        event.cancel()
        return
    }

    if (plugin.config.blockedInteracts.any { it.equals(block, ignoreCase = true) }) {
        // this interaction is blocked!
        event.cancel()
        return
    }
}
