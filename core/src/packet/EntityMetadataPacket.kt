package cat.freya.khs.packet

import cat.freya.khs.Khs
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Player
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata

data class EntityMetadataPacket(val plugin: Khs, val entity: Entity, val metadata: EntityMetadata) : Packet {
    override fun create(player: Player): WrapperPlayServerEntityMetadata {
        val list = mutableListOf<EntityData<*>>()

        // entity metadata
        list.addAll(metadata.toData())

        // set player metadata
        if (entity is Player) {
            val settings = plugin.clientSettings[entity.uuid] ?: ClientSettings()
            list.addAll(settings.toData(player))
        }

        return WrapperPlayServerEntityMetadata(entity.entityId, list)
    }
}
