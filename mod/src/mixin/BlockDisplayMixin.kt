package cat.freya.khs.mod.mixin

import net.minecraft.world.entity.Display.BlockDisplay
import net.minecraft.world.level.block.state.BlockState
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Invoker

@Mixin(BlockDisplay::class)
interface BlockDisplayMixin {
    @Invoker("setBlockState") fun setBlockState(blockState: BlockState)
}
