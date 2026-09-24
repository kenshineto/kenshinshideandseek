package cat.freya.khs.mod

import cat.freya.khs.disguise.Disguise
import cat.freya.khs.math.Vector
import cat.freya.khs.mod.mixin.BlockDisplayMixin
import cat.freya.khs.type.BlockType
import cat.freya.khs.world.Location
import java.util.UUID
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Display.BlockDisplay
import net.minecraft.world.entity.EntityTypes

class ModDisguise(val mod: KhsMod, uuid: UUID, blockType: BlockType) : Disguise(mod.khs, uuid, blockType) {
    override fun createBlock(location: Location): ModEntity? {
        val player = player ?: return null
        val worldName = player.getLocation().worldName
        val world = mod.server.getWorld(worldName) ?: return null

        val block = BlockDisplay(EntityTypes.BLOCK_DISPLAY, world.inner)
        block.setPos(location.x, location.y, location.z)

        val id = Identifier.tryParse(blockType.mcType) ?: return null
        val type = BuiltInRegistries.BLOCK.getValue(id) ?: return null
        val state = type.defaultBlockState()

        (block as BlockDisplayMixin).invokeSetBlockState(state)

        world.inner.addFreshEntity(block)

        return ModEntity(mod, block)
    }

    override fun getBlockOffset(): Vector {
        // unlike falling sand, display entities have their
        // position based from the corner, not the center
        return Vector(-0.5, 0.0, -0.5)
    }
}
