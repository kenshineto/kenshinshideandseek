package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitKhsPlayer
import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.event.JoinEvent
import cat.freya.khs.event.KickEvent
import cat.freya.khs.event.LeaveEvent
import cat.freya.khs.event.onJoin
import cat.freya.khs.event.onKick
import cat.freya.khs.event.onLeave
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent

class JoinLeaveListener(val plugin: KhsPlugin) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val bukkitPlayer = event.player ?: return
        val khsPlayer = BukkitKhsPlayer(plugin, bukkitPlayer)
        val khsEvent = JoinEvent(plugin.khs, khsPlayer)
        onJoin(khsEvent)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val bukkitPlayer = event.player ?: return
        val khsPlayer = BukkitKhsPlayer(plugin, bukkitPlayer)
        val khsEvent = LeaveEvent(plugin.khs, khsPlayer)
        onLeave(khsEvent)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerKick(event: PlayerKickEvent) {
        val bukkitPlayer = event.player ?: return
        val khsPlayer = BukkitKhsPlayer(plugin, bukkitPlayer)
        val khsEvent = KickEvent(plugin.khs, khsPlayer, event.reason ?: "")
        onKick(khsEvent)

        if (khsEvent.cancelled) event.setCancelled(true)
    }
}
