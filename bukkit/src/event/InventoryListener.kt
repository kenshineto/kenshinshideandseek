package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitKhsInventory
import cat.freya.khs.bukkit.BukkitKhsPlayer
import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.bukkit.toKhsItem
import cat.freya.khs.event.ClickEvent
import cat.freya.khs.event.CloseEvent
import cat.freya.khs.event.onClick
import cat.freya.khs.event.onClose
import org.bukkit.entity.Player as BukkitPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.inventory.Inventory

class InventoryListener(val plugin: KhsPlugin) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun getInv(event: InventoryEvent): Pair<Inventory, String?> {
        if (plugin.shim.supports(14)) {
            var inv = event.view.topInventory
            return inv to event.view.title
        } else {
            var inv = event.inventory
            var title = inv::class.java.getMethod("getName").invoke(inv) as String
            return inv to title
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        val (inventory, title) = getInv(event)
        val bukkitPlayer = event.whoClicked as? BukkitPlayer ?: return
        val item = toKhsItem(event.currentItem) ?: return

        val khsPlayer = BukkitKhsPlayer(plugin, bukkitPlayer)
        val khsInventory = BukkitKhsInventory(plugin.shim, inventory, title)
        val khsEvent = ClickEvent(plugin.khs, khsPlayer, khsInventory, item)
        onClick(khsEvent)

        if (khsEvent.cancelled) event.setCancelled(true)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryClose(event: InventoryCloseEvent) {
        val (inventory, title) = getInv(event)
        val bukkitPlayer = event.player as? BukkitPlayer ?: return

        val khsPlayer = BukkitKhsPlayer(plugin, bukkitPlayer)
        val khsInventory = BukkitKhsInventory(plugin.shim, inventory, title)
        val khsEvent = CloseEvent(plugin.khs, khsPlayer, khsInventory)
        onClose(khsEvent)
    }
}
