package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitItem
import cat.freya.khs.bukkit.BukkitPlayer
import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.event.InteractEvent
import cat.freya.khs.event.UseEvent
import cat.freya.khs.event.onInteract
import cat.freya.khs.event.onUse
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class InteractListener(val plugin: KhsPlugin) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun handleUse(event: PlayerInteractEvent): Boolean {
        val bukkitPlayer = event.player
        val item = BukkitItem.wrap(event.item) ?: return false

        val khsPlayer = BukkitPlayer(plugin, bukkitPlayer)
        val khsEvent = UseEvent(plugin.khs, khsPlayer, item)
        onUse(khsEvent)

        return khsEvent.cancelled
    }

    private fun handleInteract(event: PlayerInteractEvent): Boolean {
        val bukkitPlayer = event.player
        val block = event.clickedBlock?.type?.name
        val action =
            when (event.action) {
                Action.LEFT_CLICK_BLOCK -> InteractEvent.Action.LEFT_CLICK
                Action.LEFT_CLICK_AIR -> InteractEvent.Action.LEFT_CLICK
                Action.RIGHT_CLICK_BLOCK -> InteractEvent.Action.RIGHT_CLICK
                Action.RIGHT_CLICK_AIR -> InteractEvent.Action.RIGHT_CLICK
                Action.PHYSICAL -> InteractEvent.Action.ATTACK
            }

        val khsPlayer = BukkitPlayer(plugin, bukkitPlayer)
        val khsEvent = InteractEvent(plugin.khs, khsPlayer, action, block)
        onInteract(khsEvent)

        return khsEvent.cancelled
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val cancelled = handleUse(event) || handleInteract(event)

        if (cancelled) {
            event.isCancelled = true
            event.setUseInteractedBlock(Event.Result.DENY)
        }
    }
}
