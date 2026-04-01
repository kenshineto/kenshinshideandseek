package cat.freya.khs.packet

import cat.freya.khs.player.Player
import cat.freya.khs.world.Entity
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata

data class EntityMetadataPacket(val entiy: Entity, val glow: Boolean) {
    fun send(player: Player) {
        val glowingByte = if (glow) 0x40 else 0x0
        val data = EntityData(0x0, EntityDataTypes.BYTE, glowingByte.toByte())
        val packet = WrapperPlayServerEntityMetadata(entiy.entityId, listOf(data))
        player.sendPacket(packet)
    }
}
