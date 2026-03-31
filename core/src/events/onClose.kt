package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.inv.BLOCKHUNT_TITLE_PREFIX
import cat.freya.khs.player.Inventory
import cat.freya.khs.player.Player
import kotlin.text.startsWith

data class CloseEvent(val plugin: Khs, val player: Player, val inventory: Inventory) : Event()

fun onClose(event: CloseEvent) {
    val (plugin, player, inv) = event
    val game = plugin.game

    // only block hunt matters here
    if (inv.title?.startsWith(BLOCKHUNT_TITLE_PREFIX) != true) return

    val blocks = game.map?.config?.blockHunt?.blocks ?: return
    val defaultBlock = blocks.firstOrNull() ?: return
    if (player.getDisguise() == null) player.disguise(defaultBlock)
}
