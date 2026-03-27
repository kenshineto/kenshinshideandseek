package cat.freya.khs.bukkit.packet

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
import org.bukkit.entity.Entity
import org.bukkit.entity.Player as BukkitPlayer

data class EntityDestroyPacket(val entiy: Entity) {
    fun send(player: BukkitPlayer) {
        val packet = WrapperPlayServerDestroyEntities(entiy.entityId)
        PacketEvents.getAPI().playerManager.sendPacket(player, packet)
    }
}
