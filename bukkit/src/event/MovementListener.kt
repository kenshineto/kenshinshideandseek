package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitKhsPlayer
import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.event.JumpEvent
import cat.freya.khs.event.MoveEvent
import cat.freya.khs.event.onJump
import cat.freya.khs.event.onMove
import cat.freya.khs.world.Position as KhsPosition
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Material
import org.bukkit.entity.Player as BukkitPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class MovementListener(val plugin: KhsPlugin) : Listener {

    private val prevPlayersOnGround: MutableSet<UUID> = ConcurrentHashMap.newKeySet<UUID>()

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun isOnGround(player: BukkitPlayer): Boolean {
        if (plugin.shim.supports(16, 1)) {
            val below = player.location.clone().subtract(0.0, 0.1, 0.0).block
            return below.type.isSolid
        } else {
            @Suppress("DEPRECATION")
            return player.isOnGround()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerMove(event: PlayerMoveEvent) {
        val bukkitPlayer = event.player ?: return
        val khsPlayer = BukkitKhsPlayer(plugin.shim, bukkitPlayer)

        // check jumping
        if (bukkitPlayer.velocity.y > 0.0) {
            val block = bukkitPlayer.location?.block?.type
            if (
                block != Material.LADDER &&
                    prevPlayersOnGround.contains(bukkitPlayer.uniqueId) &&
                    isOnGround(bukkitPlayer)
            ) {
                // trigger jump event
                val khsEvent = JumpEvent(plugin.khs, khsPlayer)
                onJump(khsEvent)
            }
            // update set
            if (isOnGround(bukkitPlayer)) prevPlayersOnGround.add(bukkitPlayer.uniqueId)
            else prevPlayersOnGround.remove(bukkitPlayer.uniqueId)
        }

        val to = event.to?.let { KhsPosition(it.x, it.y, it.z) } ?: return
        val khsEvent = MoveEvent(plugin.khs, khsPlayer, to)
        onMove(khsEvent)

        if (khsEvent.cancelled) event.setCancelled(true)

        // update disguise (if exists)
        val disguise = plugin.disguiser.getDisguise(bukkitPlayer) ?: return
        val dest = event.to ?: return
        if (!khsEvent.cancelled && event.from.distance(dest) > 0.1) disguise.shouldBeSolid = false
        disguise.startSolidifying()
    }
}
