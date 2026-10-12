package cat.freya.khs.packet

import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes

/** Metadata that is set per entity */
data class EntityMetadata(var glowing: Boolean = false) {
    fun toData(): List<EntityData<*>> {
        val list = mutableListOf<EntityData<*>>()

        // glowing
        list.add(EntityData(0, EntityDataTypes.BYTE, (if (glowing) 0x40 else 0x0).toByte()))

        return list
    }
}
