package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitPlayer
import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.event.DamageEvent
import cat.freya.khs.event.onDamage
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

class DamageListener(val plugin: KhsPlugin) : Listener {
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun getAttacker(event: EntityDamageEvent): org.bukkit.entity.Player? {
        val damager = (event as? EntityDamageByEntityEvent)?.damager ?: return null
        return when {
            damager is Projectile -> damager.shooter as? org.bukkit.entity.Player
            else -> damager as? org.bukkit.entity.Player
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityDamage(event: EntityDamageEvent) {
        val bukkitPlayer = (event.entity as? org.bukkit.entity.Player) ?: return
        val attackerPlayer = getAttacker(event)

        val khsPlayer = BukkitPlayer(plugin, bukkitPlayer)
        val khsAttacker = attackerPlayer?.let { BukkitPlayer(plugin, it) }
        val khsEvent = DamageEvent(plugin.khs, khsPlayer, khsAttacker, event.damage)
        onDamage(khsEvent)

        if (khsEvent.cancelled) event.isCancelled = true
    }
}
