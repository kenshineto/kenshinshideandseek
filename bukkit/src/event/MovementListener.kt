package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitPlayer
import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.event.JumpEvent
import cat.freya.khs.event.MoveEvent
import cat.freya.khs.event.onJump
import cat.freya.khs.event.onMove
import cat.freya.khs.world.Position
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MovementListener(val plugin: KhsPlugin) : Listener {
    private val prevPlayersOnGround: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun isOnGround(player: org.bukkit.entity.Player): Boolean {
        if (plugin.shim.supports(16, 1)) {
            val below =
                player.location
                    .clone()
                    .subtract(0.0, 0.1, 0.0)
                    .block
            return below.type.isSolid
        } else {
            @Suppress("DEPRECATION")
            return player.isOnGround
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerMove(event: PlayerMoveEvent) {
        val bukkitPlayer = event.player
        val khsPlayer = BukkitPlayer(plugin, bukkitPlayer)

        // check jumping
        if (bukkitPlayer.velocity.y > 0.0) {
            val block = bukkitPlayer.location.block.type
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
            if (isOnGround(bukkitPlayer)) {
                prevPlayersOnGround.add(bukkitPlayer.uniqueId)
            } else {
                prevPlayersOnGround.remove(bukkitPlayer.uniqueId)
            }
        }

        val from = event.from.let { Position(it.x, it.y, it.z) }
        val to = event.to?.let { Position(it.x, it.y, it.z) } ?: return
        val khsEvent = MoveEvent(plugin.khs, khsPlayer, from, to)
        onMove(khsEvent)

        if (khsEvent.cancelled) event.isCancelled = true
    }
}
