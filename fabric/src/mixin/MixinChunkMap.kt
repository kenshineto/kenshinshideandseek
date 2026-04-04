package cat.freya.khs.fabric.mixin

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.server.level.ChunkMap
import net.minecraft.server.level.ServerPlayer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.gen.Invoker

// thank u craft bukkit xoxoxo

@Mixin(ChunkMap::class)
public interface MixinChunkMap {
    @get:Accessor("entityMap") val entityMap: Int2ObjectMap<MixinTrackedEntity>

    interface MixinTrackedEntity {
        @Invoker("removePlayer") fun removePlayer(player: ServerPlayer)

        @Invoker("updatePlayer") fun updatePlayer(player: ServerPlayer)

        @get:Accessor("seenBy") val seenBy: MutableSet<*>
    }
}
