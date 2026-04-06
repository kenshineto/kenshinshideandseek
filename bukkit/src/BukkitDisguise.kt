package cat.freya.khs.bukkit

import cat.freya.khs.disguise.Disguise
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Location
import java.util.UUID
import org.bukkit.entity.AbstractHorse
import org.bukkit.entity.EntityType
import org.bukkit.entity.FallingBlock

class BukkitDisguise(
    private val bukkitPlugin: KhsPlugin,
    uuid: UUID,
    val bukkitMaterial: BukkitMaterial,
) : Disguise(bukkitPlugin.khs, uuid, bukkitMaterial) {

    override fun createBlock(location: Location): Entity? {
        val player = player ?: return null
        val worldName = player.getLocation().worldName
        val world = bukkitPlugin.server.getWorld(worldName) ?: return null

        val loc = org.bukkit.Location(world, location.x, location.y, location.z)
        @Suppress("DEPRECATION")
        val block: FallingBlock? =
            runCatching { world.spawnFallingBlock(loc, bukkitMaterial.inner, 0x0) }
                .getOrElse { null }
        if (block == null) return null

        if (plugin.shim.supports(10)) block.setGravity(false)

        block.dropItem = false
        block.isInvulnerable = true
        return BukkitEntity(bukkitPlugin, block)
    }

    override fun createHitBox(location: Location): Entity? {
        val player = player ?: return null
        val worldName = player.getLocation().worldName
        val world = bukkitPlugin.server.getWorld(worldName) ?: return null

        val loc = org.bukkit.Location(world, location.x, location.y, location.z)
        val hitBox: AbstractHorse? =
            if (plugin.shim.supports(11)) {
                world.spawnEntity(loc, EntityType.SKELETON_HORSE) as AbstractHorse
            } else {
                world.spawnEntity(loc, EntityType.HORSE) as AbstractHorse
            }
        if (hitBox == null) return null

        if (plugin.shim.supports(10)) hitBox.setGravity(false)

        hitBox.setAI(false)
        hitBox.isInvulnerable = true
        hitBox.canPickupItems = false
        return BukkitEntity(bukkitPlugin, hitBox)
    }
}
