package cat.freya.khs.disguise

import cat.freya.khs.player.Player
import cat.freya.khs.world.Material
import java.util.UUID
import kotlin.synchronized

class Disguiser {
    private val disguises = mutableMapOf<UUID, Disguise>()

    fun getDisguise(uuid: UUID): Disguise? = disguises[uuid]

    fun getByBlockId(id: Int): Disguise? = disguises.values.firstOrNull { it.block?.entityId == id }

    fun getByHitBoxId(id: Int): Disguise? =
        disguises.values.firstOrNull { it.hitBox?.entityId == id }

    fun disguise(player: Player, material: Material) {
        synchronized(disguises) {
            // remove old disguise (if exists)
            reveal(player.uuid)

            val disguise = player.createDisguise(material) ?: return
            disguises.put(player.uuid, disguise)
        }
    }

    fun disguiseIfNot(player: Player, material: Material) {
        synchronized(disguises) {
            if (disguises.containsKey(player.uuid)) return
            disguise(player, material)
        }
    }

    fun reveal(uuid: UUID) {
        synchronized(disguises) { disguises.remove(uuid)?.destroy() }
    }

    fun update() {
        synchronized(disguises) { disguises.values.forEach { it.update() } }
    }

    fun cleanup() {
        synchronized(disguises) { for (uuid in disguises.keys) reveal(uuid) }
    }
}
