package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitKhsPlayer
import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.event.DamageEvent
import cat.freya.khs.event.onDamage
import org.bukkit.entity.Player as BukkitPlayer
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

    private fun getAttacker(event: EntityDamageEvent): BukkitPlayer? {
        val damager = (event as? EntityDamageByEntityEvent)?.damager ?: return null
        return when {
            damager is Projectile -> damager.shooter as? BukkitPlayer
            else -> damager as? BukkitPlayer
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityDamage(event: EntityDamageEvent) {
        val bukkitPlayer = (event.entity as? BukkitPlayer) ?: return
        val attackerPlayer = getAttacker(event)

        val khsPlayer = BukkitKhsPlayer(plugin.shim, bukkitPlayer)
        val khsAttacker = attackerPlayer?.let { BukkitKhsPlayer(plugin.shim, it) }
        val khsEvent = DamageEvent(plugin.khs, khsPlayer, khsAttacker, event.damage)
        onDamage(khsEvent)

        if (khsEvent.cancelled) event.setCancelled(true)
    }
}
