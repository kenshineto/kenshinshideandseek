package cat.freya.khs.mod

import cat.freya.khs.disguise.Disguise
import cat.freya.khs.mod.mixin.BlockDisplayMixin
import cat.freya.khs.mod.mixin.DisplayMixin
import cat.freya.khs.type.BlockType
import cat.freya.khs.world.Location
import com.mojang.math.Transformation
import java.util.UUID
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Display.BlockDisplay
import net.minecraft.world.entity.EntityTypes
import org.joml.Vector3f

class ModDisguise(val mod: KhsMod, uuid: UUID, blockType: BlockType) : Disguise(mod.khs, uuid, blockType) {
    override fun createBlock(location: Location): ModEntity? {
        val player = player ?: return null
        val worldName = player.getLocation().worldName
        val world = mod.server.getWorld(worldName) ?: return null

        val block = BlockDisplay(EntityTypes.BLOCK_DISPLAY, world.inner)
        block.setPos(location.x, location.y, location.z)

        val id = Identifier.tryParse(blockType.mcType) ?: return null
        val type = BuiltInRegistries.BLOCK.get(id).orElse(null) ?: return null
        val state = type.value().defaultBlockState()

        (block as BlockDisplayMixin).`khs$setBlockState`(state)
        (block as DisplayMixin).`khs$setTransformation`(Transformation(Vector3f(-0.5f, 0f, -0.5f), null, Vector3f(1f, 1f, 1f), null))

        world.inner.addFreshEntity(block)

        return ModEntity(mod, block)
    }
}
