package cat.freya.khs.mod.mixin

import cat.freya.khs.mod.internal.SafeIterator
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Redirect

private const val TICK_CHILDREN_TARGET = "Ljava/lang/Iterableiterator()Ljava/util/Iterator;"
private const val GAME_RULE_CHANGED_TARGET =
    "Lnet/minecraft/server/players/PlayerListbroadcastAll(Lnet/minecraft/network/protocol/Packet;)V"

@Mixin(MinecraftServer::class)
abstract class MinecraftServerMixin {
    @Suppress("UNUSED_PARAMETER")
    @Redirect(
        method = ["tickChildren"],
        at = [At(value = "INVOKE", target = TICK_CHILDREN_TARGET, ordinal = 0)],
        require = 0,
    )
    private fun `khs$copyBeforeTicking`(instance: Iterable<ServerLevel>): Iterator<ServerLevel> {
        return SafeIterator(instance as Collection<ServerLevel>)
    }
}
