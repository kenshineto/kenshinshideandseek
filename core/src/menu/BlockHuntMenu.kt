package cat.freya.khs.menu

import cat.freya.khs.Khs
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.KhsMap

object BlockHuntMenu {
    const val PREFIX = "Select a Block: "

    fun create(plugin: Khs, map: KhsMap): Inventory? {
        val blocks = map.config.blockHunt.blocks

        // make inv
        val rows = (blocks.size.toUInt() + 8u) / 9u
        val size = minOf(rows * 9u, 9u)
        val inv = plugin.shim.createInventory("$PREFIX${map.name}", size) ?: return null

        // add items
        blocks
            .mapNotNull { plugin.shim.parseItem(ItemConfig(material = it)) }
            .withIndex()
            .forEach { (i, item) -> inv.set(i.toUInt(), item) }

        return inv
    }
}
