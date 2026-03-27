package cat.freya.khs.bukkit.disguise

import cat.freya.khs.bukkit.KhsPlugin
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Material
import org.bukkit.entity.Player as BukkitPlayer

class Disguiser(val plugin: KhsPlugin) {
    val disguises = ConcurrentHashMap<BukkitPlayer, Disguise>()

    fun cleanup() {
        disguises.forEach { it.value.remove() }
        disguises.clear()
    }

    fun getDisguise(player: BukkitPlayer): Disguise? = disguises.get(player)

    fun getByEntityId(id: Int): Disguise? = disguises.values.firstOrNull { it.entityId == id }

    fun getByHitboxId(id: Int): Disguise? = disguises.values.firstOrNull { it.hitBoxId == id }

    fun update() {
        for ((player, disguise) in disguises) {
            if (!player.isOnline) {
                disguise.remove()
                disguises.remove(player)
            } else {
                disguise.update()
            }
        }
    }

    fun disguise(player: BukkitPlayer, material: Material) {
        // remove old disguise (if exists)
        reveal(player)
        // make new one
        disguises.put(player, Disguise(plugin, player, material))
    }

    fun reveal(player: BukkitPlayer) {
        disguises.remove(player)?.remove()
    }
}
