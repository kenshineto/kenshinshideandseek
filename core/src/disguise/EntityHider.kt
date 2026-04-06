package cat.freya.khs.disguise

import cat.freya.khs.packet.EntityDestroyPacket
import cat.freya.khs.packet.EntityMetadataPacket
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Player
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table

class EntityHider {
    val map: Table<Int, Int, Boolean> = HashBasedTable.create()

    // set if the entity is hidden for the observer
    private fun setVisible(observer: Player, entity: Entity, visible: Boolean) = runCatching {
        val observerId = observer.entityId
        val entityId = entity.entityId
        val ret =
            if (visible) map.put(entityId, observerId, true) else map.remove(entityId, observerId)
        ret ?: visible
    }

    // is entity visible for the observer
    fun isVisible(observer: Player, entityId: Int): Boolean =
        runCatching {
                val observerId = observer.entityId
                !map.contains(observerId, entityId)
            }
            .getOrElse { true }

    // removes a player from the map
    fun removePlayer(player: Player) =
        runCatching { player.entityId }.onSuccess { map.rowMap().remove(it) }

    // hides and entity
    fun hideEntity(observer: Player, entity: Entity) {
        setVisible(observer, entity, false)

        val packet = EntityDestroyPacket(entity)
        packet.send(observer)
    }

    // unhides the entity
    fun showEntity(observer: Player, entity: Entity) {
        setVisible(observer, entity, true)

        val packet = EntityMetadataPacket(entity, false)
        packet.send(observer)
    }
}
