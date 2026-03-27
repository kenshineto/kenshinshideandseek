package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitKhsPlayer
import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.bukkit.toKhsItem
import cat.freya.khs.event.InteractEvent
import cat.freya.khs.event.UseEvent
import cat.freya.khs.event.onInteract
import cat.freya.khs.event.onUse
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class InteractListener(val plugin: KhsPlugin) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val bukkitPlayer = event.player ?: return

        val khsPlayer = BukkitKhsPlayer(plugin.shim, bukkitPlayer)

        val block = event.clickedBlock?.type?.name
        if (event.action == Action.RIGHT_CLICK_BLOCK && block != null) {
            val khsEvent = InteractEvent(plugin.khs, khsPlayer, block)
            onInteract(khsEvent)

            if (khsEvent.cancelled) {
                event.setCancelled(true)
                return
            }
        }

        val item = toKhsItem(event.item)
        if (item != null) {
            val khsEvent = UseEvent(plugin.khs, khsPlayer, item)
            onUse(khsEvent)

            if (khsEvent.cancelled) {
                event.setCancelled(true)
                return
            }
        }
    }
}
