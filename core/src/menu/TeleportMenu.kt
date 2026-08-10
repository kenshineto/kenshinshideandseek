package cat.freya.khs.menu

import cat.freya.khs.Khs
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.Game
import cat.freya.khs.type.Item
import cat.freya.khs.world.Player

object TeleportMenu {
    private fun createPageItem(plugin: Khs, page: UInt): Item? {
        val prefix = plugin.locale.menu.teleportPrefix
        val config = ItemConfig("${prefix}${page + 1u}", "ENCHANTED_BOOK")
        return plugin.parseItem(config)
    }

    private fun createPlayerItem(plugin: Khs, player: Player): Item? {
        val team = plugin.game.teams.get(player.uuid) ?: return null
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
        return plugin.parseItem(config)
    }

    fun create(plugin: Khs, page: UInt): Inventory? {
        val pageSize = 7u
        val offset = pageSize * page

        // make items
        val players = (plugin.game.teams.getSeekerPlayers() + plugin.game.teams.getHiderPlayers())
        val items =
            players.drop(offset.toInt()).take(pageSize.toInt()).mapNotNull {
                createPlayerItem(plugin, it)
            }
        val prev = if (page > 0u) createPageItem(plugin, page - 1u) else null
        val next =
            if (players.size.toUInt() > offset + pageSize) {
                createPageItem(plugin, page + 1u)
            } else {
                null
            }

        // create inv
        val title = plugin.locale.menu.teleportTitle
        val inv = plugin.shim.createInventory(title, 9u) ?: return null
        for ((i, item) in items.withIndex()) {
            inv.set(i.toUInt() + 1u, item)
        }
        if (prev != null) inv.set(0u, prev)
        if (next != null) inv.set(8u, next)

        return inv
    }

    fun onClick(plugin: Khs, player: Player, item: Item) {
        val name = item.name ?: return
        val prefix = plugin.locale.menu.teleportPrefix

        // how did you get access to this menu???
        if (!plugin.game.teams.isSpectator(player.uuid)) return

        if (item.similar("PLAYER_HEAD")) {
            player.closeInventory()

            val target = plugin.shim.getPlayer(name) ?: return
            player.teleport(target.getLocation())
        } else if (item.similar("ENCHANTED_BOOK") && name.startsWith(prefix)) {
            player.closeInventory()

            val page = name.substring(prefix.length).toUIntOrNull() ?: return
            val inv = TeleportMenu.create(plugin, page - 1u) ?: return
            player.showInventory(inv)
        }
    }
}
