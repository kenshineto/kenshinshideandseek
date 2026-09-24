package cat.freya.khs.menu

import cat.freya.khs.Khs
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.Game
import cat.freya.khs.type.Item
import cat.freya.khs.world.Player

class DebugMenu(val plugin: Khs) {
    private val becomeHider = ItemConfig("&6Become a &lHider", plugin.types.leatherChestplate)
    private val becomeSeeker = ItemConfig("&cBecome a &lSEEKER", plugin.types.goldenChestplate)
    private val becomeSpectator = ItemConfig("&8Become a &lSPECTATOR", plugin.types.ironChestplate)
    private val dieInGame = ItemConfig("&cDie in game", plugin.types.skeletonSkull)
    private val removeDisguise = ItemConfig("&cRemove disguise", plugin.types.barrier)
    private val hideSelf = ItemConfig("&cHide self", plugin.types.redWool)
    private val showSelf = ItemConfig("&cShow self", plugin.types.greenWool)
    private val tauntSelf = ItemConfig("&eTaunt self", plugin.types.firework)

    private val actions: Map<ItemConfig, (Player) -> Unit> =
        linkedMapOf(
            becomeHider to ::handleBecomeHider,
            becomeSeeker to ::handleBecomeSeeker,
            becomeSpectator to ::handleBecomeSpectator,
            dieInGame to ::handleDieInGame,
            removeDisguise to ::handleRemoveDisguise,
            hideSelf to ::handleHideSelf,
            showSelf to ::handleShowSelf,
            tauntSelf to ::handleTauntSelf,
        )

    private fun handleBecomeHider(player: Player) {
        plugin.game.loadHider(player)
    }

    private fun handleBecomeSeeker(player: Player) {
        plugin.game.loadSeeker(player)
    }

    private fun handleBecomeSpectator(player: Player) {
        plugin.game.loadSpectator(player)
    }

    private fun handleDieInGame(player: Player) {
        val team = plugin.game.teams.get(player.uuid)
        if (team == null || team == Game.Team.SPECTATOR) return
        if (plugin.game.status != Game.Status.SEEKING) return
        player.setHealth(0.1)
    }

    private fun handleRemoveDisguise(player: Player) {
        plugin.disguiser.reveal(player.uuid)
    }

    private fun handleHideSelf(player: Player) {
        plugin.entityHider.hideEntity(player, player.uuid)
    }

    private fun handleShowSelf(player: Player) {
        plugin.entityHider.showEntity(player)
    }

    private fun handleTauntSelf(player: Player) {
        player.taunt()
    }

    fun create(): Inventory? {
        val title = plugin.locale.menu.debugTitle
        val inv = plugin.shim.createInventory(title, 9u) ?: return null
        actions.keys.mapNotNull { plugin.parseItem(it) }.withIndex().forEach { (i, item) -> inv.set(i.toUInt(), item) }
        return inv
    }

    fun onClick(player: Player, item: Item) {
        if (!player.hasPermission("hs.debug")) return
        val (_, fn) = actions.entries.firstOrNull { (config, _) -> item.similar(config) } ?: return
        fn(player)
        player.closeInventory()
    }
}
