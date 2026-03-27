package cat.freya.khs.bukkit.disguise

import cat.freya.khs.bukkit.packet.EntityDestroyPacket
import cat.freya.khs.bukkit.packet.EntityMetadataPacket
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import org.bukkit.entity.Entity as BukkitEntity
import org.bukkit.entity.Player as BukkitPlayer

class EntityHider {
    val map: Table<Int, Int, Boolean> = HashBasedTable.create()

    // is entity visible for the observer
    fun isVisible(observer: BukkitPlayer, entityId: Int): Boolean =
        runCatching {
                val observerId = observer.entityId
                !map.contains(observerId, entityId)
            }
            .getOrElse { true }

    // is entity visible for the observer
    fun isVisible(observer: BukkitPlayer, entity: BukkitEntity): Boolean =
        runCatching { isVisible(observer, entity.entityId) }.getOrElse { true }

    // set if the entity is hidden for the observer
    fun setVisible(observer: BukkitPlayer, entity: BukkitEntity, visible: Boolean) = runCatching {
        val observerId = observer.entityId
        val entityId = entity.entityId
        val ret =
            if (visible) map.put(entityId, observerId, true) else map.remove(entityId, observerId)
        ret ?: visible
    }

    // removes a hidden entity from the map
    fun removeEntity(entity: BukkitEntity) =
        runCatching { entity.entityId }
            .onSuccess { for (row in map.rowMap().values) row.remove(it) }

    // removes a player from the map
    fun removePlayer(player: BukkitPlayer) =
        runCatching { player.entityId }.onSuccess { map.rowMap().remove(it) }

    // hides and entity
    fun hideEntity(observer: BukkitPlayer, entity: BukkitEntity) {
        setVisible(observer, entity, false)

        val packet = EntityDestroyPacket(entity)
        packet.send(observer)
    }

    // unhides the entity
    fun showEntity(observer: BukkitPlayer, entity: BukkitEntity) {
        setVisible(observer, entity, true)

        val packet = EntityMetadataPacket(entity, false)
        packet.send(observer)
    }
}
