package cat.freya.khs.bukkit

import cat.freya.khs.disguise.Disguise as KhsDisguise
import cat.freya.khs.world.Entity as KhsEntity
import cat.freya.khs.world.Location
import cat.freya.khs.world.Material as KhsMaterial
import java.util.UUID
import org.bukkit.Material
import org.bukkit.entity.AbstractHorse
import org.bukkit.entity.EntityType
import org.bukkit.entity.FallingBlock

class Disguise(private val bukkitPlugin: KhsPlugin, uuid: UUID, material: KhsMaterial) :
    KhsDisguise(bukkitPlugin.khs, uuid, material) {

    override fun createBlock(location: Location): KhsEntity? {
        val player = player ?: return null
        val worldName = player.location.worldName
        val world = bukkitPlugin.server.getWorld(worldName) ?: return null

        val loc = org.bukkit.Location(world, location.x, location.y, location.z)
        val bukkitMaterial = Material.getMaterial(material.platformName) ?: return null
        @Suppress("DEPRECATION")
        val block: FallingBlock? =
            runCatching { world.spawnFallingBlock(loc, bukkitMaterial, 0x0) }.getOrElse { null }
        if (block == null) return null

        if (plugin.shim.supports(10)) block.setGravity(false)

        block.dropItem = false
        block.isInvulnerable = true
        return BukkitKhsEntity(bukkitPlugin, block)
    }

    override fun createHitBox(location: Location): KhsEntity? {
        val player = player ?: return null
        val worldName = player.location.worldName
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
        return BukkitKhsEntity(bukkitPlugin, hitBox)
    }
}
