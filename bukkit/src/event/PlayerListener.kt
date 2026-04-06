package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitItem
import cat.freya.khs.bukkit.BukkitPlayer
import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.event.DropEvent
import cat.freya.khs.event.HungerEvent
import cat.freya.khs.event.RegenEvent
import cat.freya.khs.event.onDrop
import cat.freya.khs.event.onHunger
import cat.freya.khs.event.onRegen
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerDropItemEvent

class PlayerListener(val plugin: KhsPlugin) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        val bukkitPlayer = event.entity as? org.bukkit.entity.Player ?: return
        val khsPlayer = BukkitPlayer(plugin, bukkitPlayer)
        val khsEvent = HungerEvent(plugin.khs, khsPlayer)
        onHunger(khsEvent)

        if (khsEvent.cancelled) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityRegainHealth(event: EntityRegainHealthEvent) {
        val bukkitPlayer = event.entity as? org.bukkit.entity.Player ?: return
        val khsPlayer = BukkitPlayer(plugin, bukkitPlayer)
        val natural =
            event.regainReason == EntityRegainHealthEvent.RegainReason.SATIATED ||
                event.regainReason == EntityRegainHealthEvent.RegainReason.REGEN
        val khsEvent = RegenEvent(plugin.khs, khsPlayer, natural)
        onRegen(khsEvent)

        if (khsEvent.cancelled) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        val bukkitPlayer = event.player
        val item = BukkitItem.wrap(event.itemDrop.itemStack) ?: return
        val khsPlayer = BukkitPlayer(plugin, bukkitPlayer)
        val khsEvent = DropEvent(plugin.khs, khsPlayer, item)
        onDrop(khsEvent)

        if (khsEvent.cancelled) event.isCancelled = true
    }
}
