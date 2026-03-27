package cat.freya.khs.inv

import cat.freya.khs.Khs
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.Game
import cat.freya.khs.player.Inventory
import cat.freya.khs.player.Player

const val DEBUG_TITLE = "Teleport"
val BECOME_HIDER = ItemConfig("&6Become a &lHider", "LEATHER_CHESTPLATE")
val BECOME_SEEKER = ItemConfig("&cBecome a &lSEEKER", "GOLDEN_CHESTPLATE")
val BECOME_SPECTATOR = ItemConfig("&8Become a &lSPECTATOR", "IRON_CHESTPLATE")
val DIE_IN_GAME = ItemConfig("&cDie in game", "SKELETON_SKULL")
val REVEAL_DISGUISE = ItemConfig("&cReveal disguise", "BARRIER")

fun becomeHider(plugin: Khs, player: Player) {
    plugin.game.setTeam(player.uuid, Game.Team.HIDER)
    plugin.game.loadHider(player)
    if (plugin.game.status == Game.Status.SEEKING) plugin.game.giveHiderItems(player)
}

fun becomeSeeker(plugin: Khs, player: Player) {
    plugin.game.setTeam(player.uuid, Game.Team.SEEKER)
    plugin.game.loadSeeker(player)
    if (plugin.game.status == Game.Status.SEEKING) plugin.game.giveSeekerItems(player)
}

fun becomeSpectator(plugin: Khs, player: Player) {
    plugin.game.setTeam(player.uuid, Game.Team.SPECTATOR)
    plugin.game.loadSpectator(player)
}

fun dieInGame(plugin: Khs, player: Player) {
    val team = plugin.game.getTeam(player.uuid)
    if (team == null || team == Game.Team.SPECTATOR) return
    if (plugin.game.status != Game.Status.SEEKING) return
    player.health = 0.1
}

fun createDebugMenu(plugin: Khs): Inventory? {
    val inv = plugin.shim.createInventory(DEBUG_TITLE, 9u) ?: return null
    val items = listOf(BECOME_HIDER, BECOME_SEEKER, BECOME_SPECTATOR, DIE_IN_GAME, REVEAL_DISGUISE)
    items
        .map { plugin.shim.parseItem(it) }
        .filterNotNull()
        .withIndex()
        .forEach { (i, item) -> inv.set(i.toUInt(), item) }
    return inv
}
