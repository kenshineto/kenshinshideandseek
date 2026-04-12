package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitInventory
import cat.freya.khs.bukkit.BukkitItem
import cat.freya.khs.bukkit.BukkitPlayer
import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.event.ClickEvent
import cat.freya.khs.event.CloseEvent
import cat.freya.khs.event.onClick
import cat.freya.khs.event.onClose
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

    private fun getInventory(event: InventoryEvent): Pair<Inventory, String?> {
        if (plugin.shim.supports(14)) {
            val inv = event.view.topInventory
            return inv to event.view.title
        } else {
            val inv = event.inventory
            val title = inv::class.java.getMethod("getName").invoke(inv) as String
            return inv to title
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        val (inventory, title) = getInventory(event)
        val bukkitPlayer = event.whoClicked as? org.bukkit.entity.Player ?: return
        val item = BukkitItem.wrap(event.currentItem) ?: return

        val khsPlayer = BukkitPlayer(plugin, bukkitPlayer)
        val khsInventory = BukkitInventory(plugin.shim, inventory, title)
        val khsEvent = ClickEvent(plugin.khs, khsPlayer, khsInventory, item)
        onClick(khsEvent)

        if (khsEvent.cancelled) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryClose(event: InventoryCloseEvent) {
        val (inventory, title) = getInventory(event)
        val bukkitPlayer = event.player as? org.bukkit.entity.Player ?: return

        val khsPlayer = BukkitPlayer(plugin, bukkitPlayer)
        val khsInventory = BukkitInventory(plugin.shim, inventory, title)
        val khsEvent = CloseEvent(plugin.khs, khsPlayer, khsInventory)
        onClose(khsEvent)
    }
}
