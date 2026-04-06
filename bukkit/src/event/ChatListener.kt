package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitPlayer
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
        val bukkitPlayer = event.player
        val message = event.message

        val khsPlayer = BukkitPlayer(plugin, bukkitPlayer)
        val khsEvent = ChatEvent(plugin.khs, khsPlayer, message)
        onChat(khsEvent)

        if (khsEvent.cancelled) event.isCancelled = true
    }
}
