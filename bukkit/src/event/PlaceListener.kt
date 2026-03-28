package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.KhsPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityChangeBlockEvent

class PlaceListener(val plugin: KhsPlugin) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityChangeBlock(event: EntityChangeBlockEvent) {
        val entity = event.entity
        val disguise = plugin.disguiser.getByEntityId(entity.entityId)
        if (disguise != null) event.setCancelled(true)
    }
}
