package cat.freya.khs.packet

import cat.freya.khs.world.Entity
import cat.freya.khs.world.Player
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity
import java.util.Optional

data class EntitySpawnPacket(val entity: Entity) {

    private val packet: WrapperPlayServerSpawnEntity

    init {
        val entityId = entity.entityId
        val uuid = entity.uuid

        val typeName = entity.type.minecraftKey
        val typeId = entity.type.minecraftId

        // get type by id or namespaced key
        val type =
            when {
                typeName != null -> EntityTypes.getByName(typeName)
                typeId != null -> EntityTypes.getById(null, typeId)
                else -> null
            }

        // invalid entity
        if (type == null) error("entity does not have a type!")

        val khsPosition = entity.getLocation()
        val khsVelocity = entity.getVelocity()

        val position = Vector3d(khsPosition.x, khsPosition.y, khsPosition.z)
        val pitch = entity.getPitch()
        val yaw = entity.getYaw()
        val headYaw = entity.getHeadYaw() ?: 0f
        val velocity = Vector3d(khsVelocity.x, khsVelocity.y, khsVelocity.z)

        packet =
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
    }

    fun send(player: Player) {
        player.sendPacket(packet)
    }
}
