package cat.freya.khs.menu

import cat.freya.khs.Khs
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.Game
import cat.freya.khs.type.Item
import cat.freya.khs.world.Player

object DebugMenu {
    private val BECOME_HIDER = ItemConfig("&6Become a &lHider", "LEATHER_CHESTPLATE")
    private val BECOME_SEEKER = ItemConfig("&cBecome a &lSEEKER", "GOLDEN_CHESTPLATE")
    private val BECOME_SPECTATOR = ItemConfig("&8Become a &lSPECTATOR", "IRON_CHESTPLATE")
    private val DIE_IN_GAME = ItemConfig("&cDie in game", "SKELETON_SKULL")
    private val REMOVE_DISGUISE = ItemConfig("&cRemove disguise", "BARRIER")
    private val HIDE_SELF = ItemConfig("&cHide self", "RED_WOOL")
    private val SHOW_SELF = ItemConfig("&cShow self", "GREEN_WOOL")

    private val ACTIONS: Map<ItemConfig, (Khs, Player) -> Unit> =
        linkedMapOf(
            BECOME_HIDER to ::handleBecomeHider,
            BECOME_SEEKER to ::handleBecomeSeeker,
            BECOME_SPECTATOR to ::handleBecomeSpectator,
            DIE_IN_GAME to ::handleDieInGame,
            REMOVE_DISGUISE to ::handleRemoveDisguise,
            HIDE_SELF to ::handleHideSelf,
            SHOW_SELF to ::handleShowSelf,
        )

    private fun handleBecomeHider(plugin: Khs, player: Player) {
        plugin.game.loadHider(player)
    }

    private fun handleBecomeSeeker(plugin: Khs, player: Player) {
        plugin.game.loadSeeker(player)
    }

    private fun handleBecomeSpectator(plugin: Khs, player: Player) {
        plugin.game.loadSpectator(player)
    }

    private fun handleDieInGame(plugin: Khs, player: Player) {
        val team = plugin.game.teams.get(player.uuid)
        if (team == null || team == Game.Team.SPECTATOR) return
        if (plugin.game.status != Game.Status.SEEKING) return
        player.setHealth(0.1)
    }

    private fun handleRemoveDisguise(plugin: Khs, player: Player) {
        plugin.disguiser.reveal(player.uuid)
    }

    private fun handleHideSelf(plugin: Khs, player: Player) {
        plugin.entityHider.hideEntity(player, player.uuid)
    }

    private fun handleShowSelf(plugin: Khs, player: Player) {
        plugin.entityHider.showEntity(player)
    }

    fun create(plugin: Khs): Inventory? {
        val title = plugin.locale.menu.debugTitle
        val inv = plugin.shim.createInventory(title, 9u) ?: return null
        ACTIONS.keys.mapNotNull { plugin.parseItem(it) }.withIndex().forEach { (i, item) -> inv.set(i.toUInt(), item) }
        return inv
    }

    fun onClick(plugin: Khs, player: Player, item: Item) {
        if (!player.hasPermission("hs.debug")) return
        val (_, fn) = ACTIONS.entries.firstOrNull { (config, _) -> item.similar(config) } ?: return
        fn(plugin, player)
        player.closeInventory()
    }
}
