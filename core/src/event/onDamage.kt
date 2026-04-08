package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.game.Game
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

    if (game.hasPlayer(player)) return true

    if (attacker != null && game.hasPlayer(attacker)) return true

    return false
}

/** Checks if the attacker (if exists) is allowed to attack the player */
private fun isAttackAllowed(event: DamageEvent): Boolean {
    val (plugin, player, attacker, _) = event
    val game = plugin.game

    if (!game.hasPlayer(player)) return false

    if (game.status != Game.Status.SEEKING) return false

    if (attacker == null) {
        // assume natural causes
        if (!plugin.config.pvp && !plugin.config.allowNaturalCauses) return false

        return true
    }

    // attackers must be in the game to attack the player
    if (!game.hasPlayer(attacker)) return false

    // players cannot attack their team-mates
    if (game.getTeam(player.uuid) == game.getTeam(attacker.uuid)) return false

    // spectators cannot attack/be attacked
    if (game.isSpectator(player) || game.isSpectator(attacker)) return false

    // ignore if pvp is disabled, and a hider is trying to attack a seeker
    if (!plugin.config.pvp && game.isHider(attacker) && game.isSeeker(player)) return false

    return true
}

private fun respawnPlayer(event: DamageEvent) {
    val (plugin, player, _, _) = event
    val game = plugin.game

    if (game.isHider(player) && plugin.config.respawnAsSpectator) {
        game.setTeam(player.uuid, Game.Team.SPECTATOR)
        game.loadSpectator(player)
        return
    }

    // respawn as a seeker
    game.setTeam(player.uuid, Game.Team.SEEKER)
    game.resetPlayer(player)
    game.giveSeekerItems(player)

    // teleport
    if (plugin.config.delayedRespawn.enabled) {
        val time = plugin.config.delayedRespawn.delay
        player.teleport(game.map?.seekerLobbySpawn)
        player.message(plugin.locale.prefix.default + plugin.locale.game.respawn.with(time))
        plugin.shim.scheduleEvent(time * 20UL) {
            if (game.status == Game.Status.SEEKING) {
                player.teleport(game.map?.gameSpawn)
            }
        }
    } else {
        player.teleport(game.map?.gameSpawn)
    }
}

private fun broadcastDeath(event: DamageEvent) {
    val (plugin, player, attacker, _) = event
    val game = plugin.game

    val msg =
        if (game.isSeeker(player)) {
            plugin.locale.game.player.death.with(player.name)
        } else if (attacker == null) {
            plugin.locale.game.player.found.with(player.name)
        } else {
            plugin.locale.game.player.foundBy.with(player.name, attacker.name)
        }

    game.broadcast(msg)
}

/** the attack is valid, handle it */
private fun handleAttack(event: DamageEvent) {
    val (plugin, player, attacker, _) = event
    val game = plugin.game

    // play death sound
    player.playSound(
        if (plugin.shim.supports(9)) "ENTITY_PLAYER_DEATH" else "ENTITY_PLAYER_HURT",
        1.0,
        1.0,
    )

    // un solidify a player if their disguised
    plugin.disguiser.getDisguise(player.uuid)?.shouldBeSolid = false

    // update leaderboard
    game.addDeath(player.uuid)
    if (attacker != null) game.addKill(attacker.uuid)

    broadcastDeath(event)
    respawnPlayer(event)
}

/// handles when a player in the game is damaged
fun onDamage(event: DamageEvent) {
    val (plugin, player, _, damage) = event
    val game = plugin.game

    if (!eventHasJurisdiction(event)) return

    if (!isAttackAllowed(event)) {
        event.cancel()

        // handle spectator taking damage
        if (game.isSpectator(player)) {
            val minY = player.getWorld()?.minY ?: 0
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

    /* handle death event (player was tagged or killed in pvp) */
    event.cancel()
    handleAttack(event)
}
