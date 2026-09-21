package cat.freya.khs.mod

import cat.freya.khs.disguise.Disguise
import cat.freya.khs.type.Material
import cat.freya.khs.world.Location
import java.util.UUID
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.item.FallingBlockEntity

class ModDisguise(val mod: KhsMod, uuid: UUID, material: Material) : Disguise(mod.khs, uuid, material) {
    override fun createBlock(location: Location): ModEntity? {
        val player = player ?: return null
        val worldName = player.getLocation().worldName
        val world = mod.server.getWorld(worldName) ?: return null

        val pos = BlockPos(location.x.toInt(), location.y.toInt(), location.z.toInt())
        val key = (material as? ModBlockMaterial)?.mcKey ?: return null
        val type = BuiltInRegistries.BLOCK.getValue(key) ?: return null
        val state = type.defaultBlockState()
        val block = FallingBlockEntity.fall(world.inner, pos, state)

        // TODO: gravity, invunerable, no drop item

        return ModEntity(mod, block)
    }
}
