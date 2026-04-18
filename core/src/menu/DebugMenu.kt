package cat.freya.khs.menu

import cat.freya.khs.Khs
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.Game
import cat.freya.khs.world.Inventory
import cat.freya.khs.world.Player

object DebugMenu {
    const val TITLE = "Debug Menu"
    val BECOME_HIDER = ItemConfig("&6Become a &lHider", "LEATHER_CHESTPLATE")
    val BECOME_SEEKER = ItemConfig("&cBecome a &lSEEKER", "GOLDEN_CHESTPLATE")
    val BECOME_SPECTATOR = ItemConfig("&8Become a &lSPECTATOR", "IRON_CHESTPLATE")
    val DIE_IN_GAME = ItemConfig("&cDie in game", "SKELETON_SKULL")
    val REMOVE_DISGUISE = ItemConfig("&cRemove disguise", "BARRIER")
    val HIDE_SELF = ItemConfig("&cHide self", "RED_WOOL")
    val SHOW_SELF = ItemConfig("&cShow self", "GREEN_WOOL")

    fun handleBecomeHider(plugin: Khs, player: Player) {
        plugin.game.teams.put(player.uuid, Game.Team.HIDER)
        plugin.game.loadHider(player)
        if (plugin.game.status == Game.Status.SEEKING) plugin.game.giveHiderItems(player)
    }

    fun handleBecomeSeeker(plugin: Khs, player: Player) {
        plugin.game.teams.put(player.uuid, Game.Team.SEEKER)
        plugin.game.loadSeeker(player)
        if (plugin.game.status == Game.Status.SEEKING) plugin.game.giveSeekerItems(player)
    }

    fun handleBecomeSpectator(plugin: Khs, player: Player) {
        plugin.game.teams.put(player.uuid, Game.Team.SPECTATOR)
        plugin.game.loadSpectator(player)
    }

    fun handleDieInGame(plugin: Khs, player: Player) {
        val team = plugin.game.teams.get(player.uuid)
        if (team == null || team == Game.Team.SPECTATOR) return
        if (plugin.game.status != Game.Status.SEEKING) return
        player.setHealth(0.1)
    }

    fun create(plugin: Khs): Inventory? {
        val inv = plugin.shim.createInventory(TITLE, 9u) ?: return null
        val items =
            listOf(
                BECOME_HIDER,
                BECOME_SEEKER,
                BECOME_SPECTATOR,
                DIE_IN_GAME,
                REMOVE_DISGUISE,
                HIDE_SELF,
                SHOW_SELF,
            )
        items
            .mapNotNull { plugin.shim.parseItem(it) }
            .withIndex()
            .forEach { (i, item) -> inv.set(i.toUInt(), item) }
        return inv
    }
}
