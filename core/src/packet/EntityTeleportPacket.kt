package cat.freya.khs.packet

import cat.freya.khs.world.Entity
import cat.freya.khs.world.Location
import cat.freya.khs.world.Player
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport

data class EntityTeleportPacket(val entity: Entity, val location: Location) : Packet {
    override fun send(player: Player) {
        val vector = Vector3d(location.x, location.y, location.z)
        val yaw = entity.getYaw()
        val pitch = entity.getPitch()
        val onGround = false
        val packet = WrapperPlayServerEntityTeleport(entity.entityId, vector, yaw, pitch, onGround)
        player.sendPacket(packet)
    }
}
