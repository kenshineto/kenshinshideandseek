package cat.freya.khs.inv

import cat.freya.khs.Khs
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.Game
import cat.freya.khs.player.Inventory
import cat.freya.khs.player.Player
import cat.freya.khs.world.Item

const val TELEPORT_TITLE = "Teleport to players"

fun createPageItem(plugin: Khs, page: UInt): Item? {
    val config = ItemConfig("Page ${page + 1u}", "ENCHANTED_BOOK")
    return plugin.shim.parseItem(config)
}

fun createPlayerItem(plugin: Khs, player: Player): Item? {
    val team = plugin.game.getTeam(player.uuid) ?: return null
    val teamName =
        when (team) {
            Game.Team.HIDER -> plugin.locale.game.team.hider
            Game.Team.SEEKER -> plugin.locale.game.team.seeker
            else -> ""
        }
    val config =
        ItemConfig(
            name = player.name,
            material = "PLAYER_HEAD",
            owner = player.name,
            lore = listOf(teamName),
        )
    return plugin.shim.parseItem(config)
}

fun createTeleportMenu(plugin: Khs, page: UInt): Inventory? {
    val pageSize = 7u
    val offset = pageSize * page

    // make items
    val players = (plugin.game.seekerPlayers + plugin.game.hiderPlayers)
    val items =
        players.drop(offset.toInt()).take(pageSize.toInt()).mapNotNull {
            createPlayerItem(plugin, it)
        }
    val prev = if (page > 0u) createPageItem(plugin, page - 1u) else null
    val next =
        if (players.size.toUInt() > offset + pageSize) createPageItem(plugin, page + 1u) else null

    // create inv
    val inv = plugin.shim.createInventory(TELEPORT_TITLE, 9u) ?: return null
    for ((i, item) in items.withIndex()) {
        inv.set(i.toUInt() + 1u, item)
    }
    if (prev != null) inv.set(0u, prev)
    if (next != null) inv.set(8u, next)

    return inv
}
