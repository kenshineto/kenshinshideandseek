package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.menu.Inventory
import cat.freya.khs.world.Player
import kotlin.text.startsWith

data class CloseEvent(val plugin: Khs, val player: Player, val inventory: Inventory) : Event()

fun onClose(event: CloseEvent) {
    val (plugin, player, inv) = event
    val game = plugin.game

    // only block hunt matters here
    if (inv.title?.startsWith(plugin.locale.menu.blockHuntPrefix) != true) return

    // ignore seekers closing inventory, this fixes a race condition
    // when the inventory is closed during a team switch
    // from hider -> seeker. seekers should not be disguised
    if (game.teams.isHider(player.uuid) == false) return

    val blocks = game.map?.config?.blockHunt?.blocks ?: return
    val defaultBlock = blocks.firstOrNull() ?: return
    val blockType = plugin.parseBlock(defaultBlock) ?: return
    plugin.disguiser.disguiseIfNot(player, blockType)
}
