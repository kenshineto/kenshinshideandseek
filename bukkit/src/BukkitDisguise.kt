package cat.freya.khs.bukkit

import cat.freya.khs.disguise.Disguise
import cat.freya.khs.type.BlockType
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Location
import com.cryptomorin.xseries.XMaterial
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import org.bukkit.entity.FallingBlock

class BukkitDisguise(
    private val bukkitPlugin: KhsPlugin,
    uuid: UUID,
    blockType: BlockType,
) : Disguise(bukkitPlugin.khs, uuid, blockType) {
    override fun createBlock(location: Location): Entity? {
        val player = player ?: return null
        val worldName = player.getLocation().worldName
        val world = bukkitPlugin.server.getWorld(worldName) ?: return null

        val loc = org.bukkit.Location(world, location.x, location.y, location.z)
        val material = XMaterial.matchXMaterial(blockType.platformType).getOrNull()?.get() ?: return null

        @Suppress("DEPRECATION")
        val block: FallingBlock? = runCatching { world.spawnFallingBlock(loc, material, 0x0) }.getOrElse { null }
        if (block == null) return null

        if (plugin.shim.supports(10)) block.setGravity(false)

        block.dropItem = false
        block.isInvulnerable = true
        return BukkitEntity(bukkitPlugin, block)
    }
}
