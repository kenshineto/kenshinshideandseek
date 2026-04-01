package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitKhsPlayer
import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.event.DeathEvent
import cat.freya.khs.event.onDeath
import cat.freya.khs.world.Location
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerRespawnEvent

class RespawnListener(val plugin: KhsPlugin) : Listener {

    private val respawnLocation: MutableMap<UUID, Location> = ConcurrentHashMap<UUID, Location>()

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val bukkitPlayer = event.entity
        val khsPlayer = BukkitKhsPlayer(plugin, bukkitPlayer)
        val khsEvent = DeathEvent(plugin.khs, khsPlayer)
        onDeath(khsEvent)

        if (khsEvent.cancelled) respawnLocation[khsPlayer.uuid] = khsPlayer.location
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val bukkitPlayer = event.player
        val khsPlayer = BukkitKhsPlayer(plugin, bukkitPlayer)
        val location = respawnLocation.remove(khsPlayer.uuid) ?: return
        khsPlayer.teleport(location)
    }
}
