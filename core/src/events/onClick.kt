package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.game.Game
import cat.freya.khs.inv.*
import cat.freya.khs.player.Inventory
import cat.freya.khs.player.Player
import cat.freya.khs.world.Item
import kotlin.text.startsWith

data class ClickEvent(
    val plugin: Khs,
    val player: Player,
    val inventory: Inventory,
    val clicked: Item,
) : Event()

private fun onClickSpectator(event: ClickEvent) {
    val (plugin, player, _, item) = event
    val name = item.name ?: return
    event.cancel()

    // teleport to player
    if (item.similar("PLAYER_HEAD")) {
        player.closeInventory()

        val clicked = plugin.shim.getPlayer(name) ?: return
        player.teleport(clicked.location)
        return
    }

    // change page
    if (item.similar("ENCHANTED_BOOK") && name.startsWith("Page ")) {
        player.closeInventory()

        val page = name.substring(5).toUIntOrNull() ?: return
        val inv = createTeleportMenu(plugin, page - 1u) ?: return
        player.showInventory(inv)
    }
}

private fun onClickDebug(event: ClickEvent) {
    val (plugin, player, _, item) = event
    event.cancel()

    if (item.similar(BECOME_SEEKER)) becomeSeeker(plugin, player)
    else if (item.similar(BECOME_HIDER)) becomeHider(plugin, player)
    else if (item.similar(BECOME_SPECTATOR)) becomeSpectator(plugin, player)
    else if (item.similar(DIE_IN_GAME)) dieInGame(plugin, player)
    else if (item.similar(REVEAL_DISGUISE)) player.revealDisguise() else return

    player.closeInventory()
}

private fun onClickBlockHunt(event: ClickEvent) {
    event.cancel()

    val material = event.clicked.material
    event.player.disguise(material)
    event.player.closeInventory()
}

fun onClick(event: ClickEvent) {
    val (plugin, player, inv, _) = event
    val game = plugin.game

    // dont allow interactions in the lobby
    if (game.hasPlayer(player) && game.status == Game.Status.LOBBY) event.cancel()

    if (game.isSpectator(player)) onClickSpectator(event)

    if (inv.title == DEBUG_TITLE) onClickDebug(event)

    if (inv.title?.startsWith("Select a Block: ") == true) onClickBlockHunt(event)
}
