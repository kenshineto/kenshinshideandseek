package cat.freya.khs.disguise

import cat.freya.khs.Khs
import cat.freya.khs.type.BlockType
import cat.freya.khs.world.Player
import java.util.UUID
import kotlin.synchronized

class Disguiser(val plugin: Khs) {
    private val disguises = mutableMapOf<UUID, Disguise>()
    private val debounce = mutableSetOf<UUID>()

    fun getDisguise(uuid: UUID): Disguise? = disguises[uuid]

    fun <T> mapDisguises(inner: (Disguise) -> T?): List<T> {
        synchronized(disguises) {
            return disguises.mapNotNull { inner(it.value) }
        }
    }

    fun disguise(player: Player, blockType: BlockType) {
        synchronized(disguises) {
            // remove old disguise (if exists)
            reveal(player.uuid)

            val disguise = player.createDisguise(blockType) ?: return
            disguises.put(player.uuid, disguise)
        }
    }

    fun disguiseIfNot(player: Player, blockType: BlockType) {
        synchronized(disguises) {
            if (disguises.containsKey(player.uuid)) return
            disguise(player, blockType)
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
        synchronized(debounce) { debounce.clear() }
    }

    fun isDebounced(uuid: UUID): Boolean {
        synchronized(debounce) {
            return debounce.contains(uuid)
        }
    }

    fun setDebounce(uuid: UUID) {
        synchronized(debounce) {
            debounce.add(uuid)
        }
        plugin.shim.scheduleEvent(10UL) {
            removeDebounce(uuid)
        }
    }

    fun removeDebounce(uuid: UUID) {
        synchronized(debounce) {
            debounce.remove(uuid)
        }
    }
}
