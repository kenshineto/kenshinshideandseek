package cat.freya.khs.mod.mixin

import cat.freya.khs.event.JumpEvent
import cat.freya.khs.event.onJump
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import net.minecraft.server.level.ServerPlayer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ServerPlayer::class)
abstract class ServerPlayerMixin {

    @Suppress("UNUSED_PARAMETER")
    @Inject(method = ["jumpFromGround"], at = [At("HEAD")])
    private fun onPlayerJump(ci: CallbackInfo) {
        val mod = KhsMod.INSTANCE ?: return

        @Suppress("CAST_NEVER_SUCCEEDS") val player = this as ServerPlayer
        val khsPlayer = ModPlayer(mod, player)

        val event = JumpEvent(mod.khs, khsPlayer)
        onJump(event)
    }
}
