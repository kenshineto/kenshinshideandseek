package cat.freya.khs.mod.mixin

import com.mojang.math.Transformation
import net.minecraft.world.entity.Display.BlockDisplay
import net.minecraft.world.entity.Display
import net.minecraft.world.level.block.state.BlockState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Invoker

@Mixin(BlockDisplay::class)
interface BlockDisplayMixin {
    @Invoker("setBlockState") fun `khs$setBlockState`(blockState: BlockState)
}


@Mixin(Display::class)
interface DisplayMixin {
    @Invoker("setTransformation") fun `khs$setTransformation`(transformation: Transformation)
}
