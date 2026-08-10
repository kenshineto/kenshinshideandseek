package cat.freya.khs.menu

import cat.freya.khs.Khs
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.KhsMap
import cat.freya.khs.type.Item
import cat.freya.khs.world.Player

object BlockHuntMenu {
    fun create(plugin: Khs, map: KhsMap): Inventory? {
        val blocks = map.config.blockHunt.blocks

        // make inv
        val rows = (blocks.size.toUInt() + 8u) / 9u
        val size = minOf(rows * 9u, 9u)
        val prefix = plugin.locale.menu.blockHuntPrefix
        val inv = plugin.shim.createInventory("${prefix}${map.name}", size) ?: return null

        // add items
        blocks
            .mapNotNull { plugin.parseItem(ItemConfig(material = it)) }
            .withIndex()
            .forEach { (i, item) -> inv.set(i.toUInt(), item) }

        return inv
    }

    fun onClick(plugin: Khs, player: Player, item: Item) {
        if (!plugin.game.teams.contains(player.uuid)) return

        plugin.disguiser.disguise(player, item.material)
        player.closeInventory()
    }
}
