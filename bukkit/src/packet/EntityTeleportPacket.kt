package cat.freya.khs.bukkit.packet

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player as BukkitPlayer

data class EntityTeleportPacket(val entity: Entity, val position: Location) {
    fun send(player: BukkitPlayer) {
        val vector = Vector3d(position.x, position.y, position.z)
        val yaw = 0f
        val pitch = 0f
        val onGround = false
        val packet = WrapperPlayServerEntityTeleport(entity.entityId, vector, yaw, pitch, onGround)
        PacketEvents.getAPI().playerManager.sendPacket(player, packet)
    }
}
