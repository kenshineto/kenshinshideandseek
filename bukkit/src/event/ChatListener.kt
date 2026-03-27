package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitKhsPlayer
import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.event.ChatEvent
import cat.freya.khs.event.onChat
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatListener(val plugin: KhsPlugin) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onChat(event: AsyncPlayerChatEvent) {
        val bukkitPlayer = event.player ?: return
        val message = event.message ?: return

        val khsPlayer = BukkitKhsPlayer(plugin.shim, bukkitPlayer)
        val khsEvent = ChatEvent(plugin.khs, khsPlayer, message)
        onChat(khsEvent)

        if (khsEvent.cancelled) event.setCancelled(true)
    }
}
