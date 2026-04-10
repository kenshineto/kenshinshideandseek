package cat.freya.khs.packet

import cat.freya.khs.world.Player
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer

data class PlayerSpawnPacket(val entity: Player) : Packet {
    override fun send(player: Player) {
        val khsPosition = entity.getLocation()

        val entityId = entity.entityId
        val uuid = entity.uuid
        val position = Vector3d(khsPosition.x, khsPosition.y, khsPosition.z)
        val pitch = entity.getPitch()
        val yaw = entity.getYaw()

        val packet = WrapperPlayServerSpawnPlayer(entityId, uuid, position, pitch, yaw, listOf())

        player.sendPacket(packet)
    }
}
