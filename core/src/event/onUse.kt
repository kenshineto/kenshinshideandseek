package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.game.Game
import cat.freya.khs.menu.TeleportMenu
import cat.freya.khs.type.Item
import cat.freya.khs.world.Player

data class UseEvent(val plugin: Khs, val player: Player, val item: Item) : Event()

private fun onUseLobby(event: UseEvent) {
    val (plugin, player, item) = event

    // handle leave
    if (item.similar(plugin.config.lobby.leaveItem)) {
        event.cancel()
        plugin.commandGroup.handleCommand(player, listOf("leave"))
    }

    // handle start
    if (item.similar(plugin.config.lobby.startItem)) {
        event.cancel()
        plugin.commandGroup.handleCommand(player, listOf("start"))
    }
}

private fun onUseInGame(event: UseEvent) {
    val (plugin, player, item) = event

    if (item.similar(plugin.config.glow.item) && plugin.config.glow.enabled) {
        event.cancel()
        plugin.game.glow.start()
        player.getInventory().remove(item)
    }
}

private fun onUseSpectator(event: UseEvent) {
    val (plugin, player, item) = event

    // toggle flight
    if (item.similar(plugin.config.spectatorItems.flight)) {
        event.cancel()

        // toggle flying
        player.setAllowedFlight(!player.getFlying())
        player.setFlying(player.getAllowedFlight())
        player.actionBar(
            if (player.getFlying()) {
                plugin.locale.spectator.flyingEnabled
            } else {
                plugin.locale.spectator.flyingDisabled
            },
        )
    }

    // view teleport ui
    if (item.similar(plugin.config.spectatorItems.teleport)) {
        event.cancel()

        val inv = TeleportMenu.create(plugin, 0u) ?: return
        player.showInventory(inv)
    }
}

// for a right click interaction
fun onUse(event: UseEvent) {
    val (plugin, player, _) = event
    val game = plugin.game

    if (!game.teams.contains(player.uuid)) return

    when (game.status) {
        Game.Status.LOBBY -> {
            onUseLobby(event)
        }

        Game.Status.SEEKING -> {
            onUseInGame(event)
        }

        else -> {}
    }

    if (game.teams.isSpectator(player.uuid)) onUseSpectator(event)
}
