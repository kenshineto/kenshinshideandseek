package cat.freya.khs.packet

import cat.freya.khs.world.Entity
import cat.freya.khs.world.Player
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities

data class EntityDestroyPacket(val entity: Entity) : Packet {
    override fun send(player: Player) {
        val packet = WrapperPlayServerDestroyEntities(entity.entityId)
        player.sendPacket(packet)
    }
}
