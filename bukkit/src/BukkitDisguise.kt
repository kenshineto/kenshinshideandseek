package cat.freya.khs.bukkit

import cat.freya.khs.disguise.Disguise
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Location
import org.bukkit.entity.FallingBlock
import java.util.UUID

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
}
