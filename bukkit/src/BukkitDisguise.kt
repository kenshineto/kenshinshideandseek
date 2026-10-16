package cat.freya.khs.bukkit

import cat.freya.khs.disguise.Disguise
import cat.freya.khs.type.BlockType
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Location
import com.cryptomorin.xseries.XMaterial
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import org.bukkit.Bukkit
import org.bukkit.entity.BlockDisplay
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

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

        val block: org.bukkit.entity.Entity?
        if (bukkitPlugin.shim.supports(19, 4)) {
            block =
                world.spawn(loc, BlockDisplay::class.java) {
                    it.block = Bukkit.createBlockData(material)
                    it.transformation =
                        Transformation(Vector3f(-0.5f, 0f, -0.5f), AxisAngle4f(), Vector3f(1f, 1f, 1f), AxisAngle4f())
                }
        } else {
            @Suppress("DEPRECATION")
            block = runCatching { world.spawnFallingBlock(loc, material, 0x0) }.getOrElse { null }
            if (plugin.shim.supports(10)) block?.setGravity(false)
            block?.dropItem = false
            block?.isInvulnerable = true
        }

        return block?.let { BukkitEntity(bukkitPlugin, it) }
    }
}
