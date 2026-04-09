package cat.freya.khs.packet

import cat.freya.khs.Khs
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Player
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata

data class EntityMetadataPacket(val plugin: Khs, val entity: Entity, val flags: Flags) {

    private val data: List<EntityData<*>> = getData()
    private val packet = WrapperPlayServerEntityMetadata(entity.entityId, data)

    /** The first entry on the metadata packet. This is always set on every entity */
    data class Flags(val glowing: Boolean = false) {
        fun toData(): EntityData<Byte> {
            var data = 0x0

            if (glowing) data = data.or(0x40)

            return EntityData(0, EntityDataTypes.BYTE, data.toByte())
        }
    }

    private fun getData(): List<EntityData<*>> {
        val list = mutableListOf<EntityData<*>>()

        // base entity metadata
        list.add(flags.toData())

        // set player metadata
        val player = entity as? Player
        if (player != null) {
            val settings = plugin.clientSettings[player.uuid] ?: ClientSettings()
            list.addAll(settings.toData())
        }

        return list
    }

    fun send(player: Player) {
        player.sendPacket(packet)
    }
}
