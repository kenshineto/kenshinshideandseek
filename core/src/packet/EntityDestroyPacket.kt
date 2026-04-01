package cat.freya.khs.packet

import cat.freya.khs.player.Player
import cat.freya.khs.world.Entity
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities

data class EntityDestroyPacket(val entity: Entity) {
    fun send(player: Player) {
        val packet = WrapperPlayServerDestroyEntities(entity.entityId)
        player.sendPacket(packet)
    }
}
