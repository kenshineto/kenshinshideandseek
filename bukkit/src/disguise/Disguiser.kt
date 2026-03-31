package cat.freya.khs.bukkit.disguise

import cat.freya.khs.bukkit.KhsPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Material

class Disguiser(val plugin: KhsPlugin) {
    val disguises = ConcurrentHashMap<UUID, Disguise>()

    fun cleanup() {
        disguises.forEach { it.value.remove() }
        disguises.clear()
    }

    fun getDisguise(uuid: UUID): Disguise? = disguises.get(uuid)

    fun getByEntityId(id: Int): Disguise? = disguises.values.firstOrNull { it.entityId == id }

    fun getByHitboxId(id: Int): Disguise? = disguises.values.firstOrNull { it.hitBoxId == id }

    fun update() {
        for ((uuid, disguise) in disguises) {
            val player = disguise.player
            if (player?.isOnline != true) {
                disguise.remove()
                disguises.remove(uuid)
            } else {
                disguise.update()
            }
        }
    }

    fun disguise(uuid: UUID, material: Material) {
        // remove old disguise (if exists)
        reveal(uuid)
        // make new one
        disguises.put(uuid, Disguise(plugin, uuid, material))
    }

    fun unSolidify(uuid: UUID) {
        disguises.remove(uuid)?.shouldBeSolid = false
    }

    fun reveal(uuid: UUID) {
        disguises.remove(uuid)?.remove()
    }
}
