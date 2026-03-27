package cat.freya.khs.bukkit.packet

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import org.bukkit.entity.Entity
import org.bukkit.entity.Player as BukkitPlayer

data class EntityMetadataPacket(val entiy: Entity, val glow: Boolean) {
    fun send(player: BukkitPlayer) {
        val glowingByte = if (glow) 0x40 else 0x0
        val data = EntityData(0x0, EntityDataTypes.BYTE, glowingByte.toByte())
        val packet = WrapperPlayServerEntityMetadata(entiy.entityId, listOf(data))
        PacketEvents.getAPI().playerManager.sendPacket(player, packet)
    }
}
