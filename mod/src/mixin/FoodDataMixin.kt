package cat.freya.khs.mod.mixin

import cat.freya.khs.event.HungerEvent
import cat.freya.khs.event.onHunger
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.food.FoodData
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(FoodData::class)
abstract class FoodDataMixin {

    @Inject(method = ["tick"], at = [At("HEAD")], cancellable = true)
    private fun `khs$tick`(player: ServerPlayer, ci: CallbackInfo) {
        val mod = KhsMod.INSTANCE ?: return

        val khsPlayer = ModPlayer(mod, player)
        val event = HungerEvent(mod.khs, khsPlayer)
        onHunger(event)

        if (event.cancelled) {
            ci.cancel()
        }
    }
}
