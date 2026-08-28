package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.world.Player

data class DamageEvent(
    val plugin: Khs,
    val player: Player,
    val attacker: Player?,
    val damage: Double,
) : Event()

/** If the players are not in the game, then we should not care about the event */
private fun eventHasJurisdiction(event: DamageEvent): Boolean {
    val (plugin, player, attacker, _) = event
    val game = plugin.game

    if (game.teams.contains(player.uuid)) return true

    if (attacker != null && game.teams.contains(attacker.uuid)) return true

    return false
}

/** handles when a player in the game is damaged */
fun onDamage(event: DamageEvent) {
    val (plugin, player, attacker, damage) = event
    val game = plugin.game

    if (!eventHasJurisdiction(event)) return

    if (!game.gameMode.isDamageAllowed(player, attacker)) {
        event.cancel()

        // handle spectator taking damage
        if (game.teams.isSpectator(player.uuid)) {
            val minY = plugin.shim.getMinY()
            if (player.getLocation().y < minY) {
                // make sure they don't try to kill them self to the void lol
                player.teleport(game.map?.gameSpawn)
            }
        }

        return
    }

    // check if player dies (pvp mode)
    // if not then it is fine (if so we need to handle it)
    if (plugin.config.pvp && player.getHealth() - damage >= 0.5) return

    // handle death event (player was tagged or killed in pvp)
    event.cancel()
    game.gameMode.handleDeath(player, attacker)
}
