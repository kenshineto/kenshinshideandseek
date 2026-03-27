package cat.freya.khs.inv

import cat.freya.khs.Khs
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.KhsMap
import cat.freya.khs.player.Inventory

const val BLOCKHUNT_TITLE_PREFIX = "Select a Block: "

fun createBlockHuntPicker(plugin: Khs, map: KhsMap): Inventory? {
    val blocks = map.config.blockHunt.blocks

    // make inv
    val rows = (blocks.size.toUInt() + 8u) / 9u
    val size = minOf(rows * 9u, 9u)
    val inv = plugin.shim.createInventory("$BLOCKHUNT_TITLE_PREFIX${map.name}", size) ?: return null

    // add items
    blocks
        .map { plugin.shim.parseItem(ItemConfig(material = it)) }
        .filterNotNull()
        .withIndex()
        .forEach { (i, item) -> inv.set(i.toUInt(), item) }

    return inv
}
