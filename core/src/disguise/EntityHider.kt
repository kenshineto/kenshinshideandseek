package cat.freya.khs.disguise

import cat.freya.khs.KhsShim
import cat.freya.khs.packet.EntityDestroyPacket
import cat.freya.khs.packet.EntityMetadataPacket
import cat.freya.khs.world.Entity
import java.util.UUID

class EntityHider(val shim: KhsShim) {
    /**
     * Entities in this map are INVISIBLE to everyone but their provided OWNER (which can be null)
     */
    private val map: MutableMap<Int, UUID?> = HashMap()

    // is entity visible for the observer
    fun isHidden(observer: UUID, entityId: Int): Boolean {
        synchronized(map) {
            val owner = map[entityId] ?: return false
            return owner != observer
        }
    }

    // hides and entity
    fun hideEntity(entity: Entity, owner: UUID?) {
        val wasLastHidden: Boolean

        synchronized(map) {
            val id = entity.entityId
            wasLastHidden = map.containsKey(id)
            map.put(id, owner)
        }

        if (!wasLastHidden) {
            val packet = EntityDestroyPacket(entity)
            shim.getPlayers().forEach {
                if (it.uuid == owner) return@forEach
                packet.send(it)
            }
        }
    }

    // unhides the entity
    fun showEntity(entity: Entity) {
        val wasLastHidden: Boolean

        synchronized(map) {
            val id = entity.entityId
            wasLastHidden = map.containsKey(id)
            map.remove(id)
        }

        if (wasLastHidden) {
            val packet = EntityMetadataPacket(entity, false)
            shim.getPlayers().forEach { packet.send(it) }
        }
    }
}
