package cat.freya.khs.disguise

import cat.freya.khs.packet.EntityDestroyPacket
import cat.freya.khs.packet.EntityMetadataPacket
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Player
import java.util.UUID

class EntityHider {
    /** Entities in this map are NOT visible */
    private val map: MutableMap<UUID, MutableSet<Int>> = HashMap()

    /**
     * Sets if the entity is visible or not
     *
     * @return if the entity was last hidden
     */
    private fun setHidden(observer: UUID, entityId: Int, hidden: Boolean): Boolean {
        synchronized(map) {
            val entires = map.getOrPut(observer) { mutableSetOf() }
            return if (hidden) {
                !entires.add(entityId)
            } else {
                entires.remove(entityId)
            }
        }
    }

    // is entity visible for the observer
    fun isHidden(observer: UUID, entityId: Int): Boolean {
        synchronized(map) {
            val entires = map[observer] ?: return false
            return entires.contains(entityId)
        }
    }

    // removes a player from the map
    fun removePlayer(uuid: UUID) {
        synchronized(map) { map.remove(uuid) }
    }

    // hides and entity
    fun hideEntity(observer: Player, entity: Entity) {
        val wasLastHidden = setHidden(observer.uuid, entity.entityId, true)

        if (!wasLastHidden) {
            val packet = EntityDestroyPacket(entity)
            packet.send(observer)
        }
    }

    // unhides the entity
    fun showEntity(observer: Player, entity: Entity) {
        val wasLastHidden = setHidden(observer.uuid, entity.entityId, false)

        if (wasLastHidden) {
            val packet = EntityMetadataPacket(entity, false)
            packet.send(observer)
        }
    }
}
