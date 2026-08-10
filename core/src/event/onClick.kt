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

fun onClick(event: ClickEvent) {
    val (plugin, player, inv, item) = event
    val game = plugin.game

    // don't allow interactions in the lobby
    if (game.teams.contains(player.uuid) && game.status == Game.Status.LOBBY) {
        event.cancel()
    }

    if (inv.title == plugin.locale.menu.teleportTitle) {
        TeleportMenu.onClick(plugin, player, item)
    } else if (inv.title == plugin.locale.menu.debugTitle) {
        DebugMenu.onClick(plugin, player, item)
    } else if (inv.title?.startsWith(plugin.locale.menu.blockHuntPrefix) == true) {
        BlockHuntMenu.onClick(plugin, player, item)
    } else {
        // dont cancel the event
        return
    }

    event.cancel()
}
