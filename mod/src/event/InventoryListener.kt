
package cat.freya.khs.mod.event

import cat.freya.khs.event.ClickEvent
import cat.freya.khs.event.CloseEvent
import cat.freya.khs.event.onClick
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
        PlayerEvent.OPEN_MENU.register { _, menu ->
            handleOpen(menu)
            EventResult.pass()
        }

        PlayerEvent.CLOSE_MENU.register { player, menu ->
            handleClose(player as ServerPlayer, menu)
            EventResult.pass()
        }
    }

    private fun handleOpen(menu: AbstractContainerMenu) {
        val modMenu = menu as? ModMenu ?: return
        modMenu.listeners.add { player, khsInventory, slot ->

            val khsPlayer = ModPlayer(mod, player)
            val khsItem = khsInventory.get(slot.toUInt()) ?: return@add false
            val khsEvent = ClickEvent(mod.khs, khsPlayer, khsInventory, khsItem)
            onClick(khsEvent)

            khsEvent.cancelled
        }
    }

    private fun handleClose(player: ServerPlayer, menu: AbstractContainerMenu) {
        val modMenu = menu as? ModMenu ?: return
        val khsInventory = modMenu.inv

        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = CloseEvent(mod.khs, khsPlayer, khsInventory)
        onClose(khsEvent)
    }
}
