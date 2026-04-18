package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.game.Game
import cat.freya.khs.menu.BlockHuntMenu
import cat.freya.khs.menu.DebugMenu
import cat.freya.khs.menu.Inventory
import cat.freya.khs.menu.TeleportMenu
import cat.freya.khs.type.Item
import cat.freya.khs.world.Player
import kotlin.text.startsWith

data class ClickEvent(
    val plugin: Khs,
    val player: Player,
    val inventory: Inventory,
    val clicked: Item,
) : Event()

private fun onClickTeleport(event: ClickEvent) {
    val (plugin, player, _, item) = event
    val name = item.name ?: return

    // how did you get access to this menu???
    if (!plugin.game.teams.isSpectator(player.uuid)) return

    event.cancel()

    // teleport to player
    if (item.similar("PLAYER_HEAD")) {
        player.closeInventory()

        val target = plugin.shim.getPlayer(name) ?: return
        player.teleport(target.getLocation())
        return
    }

    // change page
    if (item.similar("ENCHANTED_BOOK") && name.startsWith("Page ")) {
        player.closeInventory()

        val page = name.substring(5).toUIntOrNull() ?: return
        val inv = TeleportMenu.create(plugin, page - 1u) ?: return
        player.showInventory(inv)
    }
}

private fun onClickDebug(event: ClickEvent) {
    val (plugin, player, _, item) = event

    // uhh you should not have access to
    // this menu
    if (!player.hasPermission("hs.debug")) return

    event.cancel()

    if (item.similar(DebugMenu.BECOME_SEEKER)) {
        DebugMenu.handleBecomeSeeker(plugin, player)
    } else if (item.similar(DebugMenu.BECOME_HIDER)) {
        DebugMenu.handleBecomeHider(plugin, player)
    } else if (item.similar(DebugMenu.BECOME_SPECTATOR)) {
        DebugMenu.handleBecomeSpectator(plugin, player)
    } else if (item.similar(DebugMenu.DIE_IN_GAME)) {
        DebugMenu.handleDieInGame(plugin, player)
    } else if (item.similar(DebugMenu.REMOVE_DISGUISE)) {
        plugin.disguiser.reveal(player.uuid)
    } else if (item.similar(DebugMenu.HIDE_SELF)) {
        plugin.entityHider.hideEntity(player, player.uuid)
    } else if (item.similar(DebugMenu.SHOW_SELF)) {
        plugin.entityHider.showEntity(player)
    } else {
        return
    }

    player.closeInventory()
}

private fun onClickBlockHunt(event: ClickEvent) {
    // bro probably named a chest or something ;-;
    if (!event.plugin.game.teams
            .contains(event.player.uuid)
    ) {
        return
    }

    event.cancel()

    val material = event.clicked.material
    event.plugin.disguiser.disguise(event.player, material)
    event.player.closeInventory()
}

fun onClick(event: ClickEvent) {
    val (plugin, player, inv, _) = event
    val game = plugin.game

    // don't allow interactions in the lobby
    if (game.teams.contains(player.uuid) && game.status == Game.Status.LOBBY) {
        event.cancel()
    }

    if (inv.title == TeleportMenu.TITLE) {
        onClickTeleport(event)
    } else if (inv.title == DebugMenu.TITLE) {
        onClickDebug(event)
    } else if (inv.title?.startsWith(BlockHuntMenu.PREFIX) == true) {
        onClickBlockHunt(event)
    }
}
