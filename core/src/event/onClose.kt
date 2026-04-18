package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.menu.BlockHuntMenu
import cat.freya.khs.menu.Inventory
import cat.freya.khs.world.Player
import kotlin.text.startsWith

data class CloseEvent(val plugin: Khs, val player: Player, val inventory: Inventory) : Event()

fun onClose(event: CloseEvent) {
    val (plugin, player, inv) = event
    val game = plugin.game

    // only block hunt matters here
    if (inv.title?.startsWith(BlockHuntMenu.PREFIX) != true) return

    val blocks =
        game.map
            ?.config
            ?.blockHunt
            ?.blocks ?: return
    val defaultBlock = blocks.firstOrNull() ?: return
    val material = plugin.shim.parseMaterial(defaultBlock) ?: return
    plugin.disguiser.disguiseIfNot(player, material)
}
