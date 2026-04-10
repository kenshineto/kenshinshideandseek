package cat.freya.khs.disguise

import cat.freya.khs.Khs
import cat.freya.khs.packet.EntityDestroyPacket
import cat.freya.khs.packet.EntityMetadataPacket
import cat.freya.khs.packet.EntitySpawnPacket
import cat.freya.khs.packet.PlayerSpawnPacket
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Player
import com.github.retrooper.packetevents.protocol.player.ClientVersion
import java.util.UUID

class EntityHider(val plugin: Khs) {
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

        if (wasLastHidden) return

        val packet = EntityDestroyPacket(entity)
        plugin.shim.getPlayers().forEach {
            if (it.uuid == owner) return@forEach
            packet.send(it)
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

        if (!wasLastHidden) return

        val destroy = EntityDestroyPacket(entity)
        val spawn = EntitySpawnPacket(entity)
        val meta = EntityMetadataPacket(plugin, entity, EntityMetadataPacket.Flags())

        val entityPlayer = entity as? Player
        val spawnPlayer = entityPlayer?.let { PlayerSpawnPacket(it) }

        plugin.shim.getPlayers().forEach {
            if (entity.uuid == it.uuid) return@forEach
            val clientVersion = it.getClientVersion()

            destroy.send(it)

            // pre 1.20.2, there is a different packet just for spawning
            // players
            if (spawnPlayer != null && clientVersion.isOlderThan(ClientVersion.V_1_20_2)) {
                spawnPlayer.send(it)
            } else {
                spawn.send(it)
            }

            meta.send(it)
        }
    }
}
