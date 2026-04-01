package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitKhsPlayer
import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.event.CommandEvent
import cat.freya.khs.event.onCommand
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class CommandListener(val plugin: KhsPlugin) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerCommand(event: PlayerCommandPreprocessEvent) {
        val bukkitPlayer = event.player ?: return
        val message = event.message ?: return

        val khsPlayer = BukkitKhsPlayer(plugin, bukkitPlayer)
        val khsEvent = CommandEvent(plugin.khs, khsPlayer, message)
        onCommand(khsEvent)

        if (khsEvent.cancelled) event.setCancelled(true)
    }
}
