package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.game.Game
import cat.freya.khs.player.Player

data class DamageEvent(
    val plugin: Khs,
    val player: Player,
    val attacker: Player?,
    val damage: Double,
) : Event()

/// handles when a player in the game is damaged
fun onDamage(event: DamageEvent) {
    val (plugin, player, attacker, damage) = event
    val game = plugin.game

    // make sure that the attacker (if exists) is allowed to damage us
    if (attacker != null) {
        // players must both be in the game
        if (
            (game.hasPlayer(player) && !game.hasPlayer(attacker)) ||
                (game.hasPlayer(attacker) && !game.hasPlayer(player))
        ) {
            event.cancel()
            return
        }

        // players cant be on the same team
        if (game.sameTeam(player.uuid, attacker.uuid)) {
            event.cancel()
            return
        }

        // cannot attack spectators
        if (game.isSpectator(player) || game.isSpectator(attacker)) {
            event.cancel()
            return
        }

        // ignore if pvp is diabled, and a hider is trying to attack a seeker
        if (!plugin.config.pvp && game.isHider(attacker) && game.isSeeker(player)) {
            event.cancel()
            return
        }
        // if there is no attacker, and the player is not in game, we do not care
    } else if (!game.hasPlayer(player)) {
        return
        // if there is no attacker, it most of been by natural causes...
        // if pvp is disabled, and config doesn't allow natural causes, cancel event
    } else if (!plugin.config.pvp && !plugin.config.allowNaturalCauses) {
        event.cancel()
        return
    }

    // spectators cannot take damage
    if (game.isSpectator(player)) {
        event.cancel()
        val world = player.world ?: return
        if (player.location.y < world.minY) {
            // make sure they dont try to kill them self to the void lol
            game.map?.gameSpawn?.teleport(player)
        }
    }

    // cant take damage until seeking
    if (game.status != Game.Status.SEEKING) {
        event.cancel()
        return
    }

    // check if player dies (pvp mode)
    // if not then it is fine (if so we need to handle it)
    if (plugin.config.pvp && player.health - damage >= 0.5) return

    /* handle death event (player was tagged or killed in pvp) */
    event.cancel()

    // play death sound
    player.playSound(
        if (plugin.shim.supports(9)) "ENTITY_PLAYER_DEATH" else "ENTITY_PLAYER_HURT",
        1.0,
        1.0,
    )

    // reveal a player if their disguised
    player.revealDisguise()

    // respawn player
    if (plugin.config.delayedRespawn.enabled && !plugin.config.respawnAsSpectator) {
        val time = plugin.config.delayedRespawn.delay
        game.map?.seekerLobbySpawn?.teleport(player)
        player.message(plugin.locale.prefix.default + plugin.locale.game.respawn.with(time))
        plugin.shim.scheduleEvent(time * 20UL) {
            if (game.status == Game.Status.SEEKING) game.map?.gameSpawn?.teleport(player)
        }
    } else {
        game.map?.gameSpawn?.teleport(player)
    }

    // update leaderboard
    game.addDeath(player.uuid)
    if (attacker != null) game.addKill(attacker.uuid)

    // broadcast death and update team
    if (game.isSeeker(player)) {
        game.broadcast(plugin.locale.game.player.death.with(player.name))
    } else {
        val msg =
            if (attacker == null) {
                plugin.locale.game.player.found.with(player.name)
            } else {
                plugin.locale.game.player.foundBy.with(player.name, attacker.name)
            }
        game.broadcast(msg)

        // reset player team and items
        if (plugin.config.respawnAsSpectator) {
            game.setTeam(player.uuid, Game.Team.SPECTATOR)
            game.loadSpectator(player)
        } else {
            game.setTeam(player.uuid, Game.Team.SEEKER)
            game.resetPlayer(player)
            game.giveSeekerItems(player)
        }
    }
}
