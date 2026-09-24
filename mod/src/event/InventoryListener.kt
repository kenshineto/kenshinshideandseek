package cat.freya.khs.mod.event

import cat.freya.khs.event.CloseEvent
import cat.freya.khs.event.onClose
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModMenu
import cat.freya.khs.mod.ModPlayer
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.PlayerEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.AbstractContainerMenu

class InventoryListener(val mod: KhsMod) {
    init {
        PlayerEvent.CLOSE_MENU.register { player, menu ->
            handleClose(player as ServerPlayer, menu)
            EventResult.pass()
        }

        // PlayerEvent.OPEN_MENU is non functional for our custom
        // inventories
    }

    private fun handleClose(player: ServerPlayer, menu: AbstractContainerMenu) {
        val modMenu = menu as? ModMenu ?: return
        val khsInventory = modMenu.inv

        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = CloseEvent(mod.khs, khsPlayer, khsInventory)
        onClose(khsEvent)
    }
}
