package cat.freya.khs.packet

import cat.freya.khs.world.Entity
import cat.freya.khs.world.Player
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity
import java.util.Optional

data class EntitySpawnPacket(val entity: Entity) : Packet {
    override fun send(player: Player) {
        val khsPosition = entity.getLocation()
        val khsVelocity = entity.getVelocity()

        val entityId = entity.entityId
        val uuid = entity.uuid
        val type = entity.type.toEntityType(player) ?: return
        val position = Vector3d(khsPosition.x, khsPosition.y, khsPosition.z)
        val pitch = entity.getPitch()
        val yaw = entity.getYaw()
        val headYaw = entity.getHeadYaw() ?: 0f
        val velocity = Vector3d(khsVelocity.x, khsVelocity.y, khsVelocity.z)

        val packet =
            WrapperPlayServerSpawnEntity(
                entityId,
                Optional.of(uuid),
                type,
                position,
                pitch,
                yaw,
                headYaw,
                0,
                Optional.of(velocity),
            )

        player.sendPacket(packet)
    }
}
