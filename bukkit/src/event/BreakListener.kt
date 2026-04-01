package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitKhsPlayer
import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.event.BreakEvent
import cat.freya.khs.event.onBreak
import org.bukkit.entity.Player as BukkitPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityBreakDoorEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent

class BreakListener(val plugin: KhsPlugin) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBlockBreak(event: BlockBreakEvent) {
        val bukkitPlayer = event.player
        val block = event.block.type.name

        val khsPlayer = BukkitKhsPlayer(plugin, bukkitPlayer)
        val khsEvent = BreakEvent(plugin.khs, khsPlayer, block)
        onBreak(khsEvent)

        if (khsEvent.cancelled) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityBreakDoor(event: EntityBreakDoorEvent) {
        val bukkitPlayer = event.entity as? BukkitPlayer ?: return
        val block = event.block.type.name

        val khsPlayer = BukkitKhsPlayer(plugin, bukkitPlayer)
        val khsEvent = BreakEvent(plugin.khs, khsPlayer, block)
        onBreak(khsEvent)

        if (khsEvent.cancelled) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onHangingBreakByEntity(event: HangingBreakByEntityEvent) {
        val bukkitPlayer = event.remover as? BukkitPlayer ?: return
        val block = event.entity.type.name

        val khsPlayer = BukkitKhsPlayer(plugin, bukkitPlayer)
        val khsEvent = BreakEvent(plugin.khs, khsPlayer, block)
        onBreak(khsEvent)

        if (khsEvent.cancelled) event.isCancelled = true
    }
}
