package cat.freya.khs.disguise

import cat.freya.khs.type.Material
import cat.freya.khs.world.Player
import java.util.UUID
import kotlin.synchronized

class Disguiser {
    private val disguises = mutableMapOf<UUID, Disguise>()

    fun getDisguise(uuid: UUID): Disguise? = disguises[uuid]

    fun <T> mapDisguises(inner: (Disguise) -> T?): List<T> {
        synchronized(disguises) {
            return disguises.mapNotNull { inner(it.value) }
        }
    }

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
