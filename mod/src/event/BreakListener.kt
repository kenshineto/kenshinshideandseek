package cat.freya.khs.mod.event

import cat.freya.khs.event.BreakEvent
import cat.freya.khs.event.PlaceEvent
import cat.freya.khs.event.onBreak
import cat.freya.khs.event.onPlace
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.BlockEvent
import dev.architectury.event.events.common.InteractionEvent
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType

class BreakListener(val mod: KhsMod) {
    init {
        BlockEvent.BREAK.register { _, _, state, player ->
            val id = BuiltInRegistries.BLOCK.getKey(state.block)
            handleBreak(player as ServerPlayer, id.toString())
        }

        BlockEvent.PLACE.register { _, _, state, player ->
            val id = BuiltInRegistries.BLOCK.getKey(state.block)
            handlePlace(player as ServerPlayer, id.toString())
        }

        InteractionEvent.INTERACT_ENTITY.register { player, entity, _ ->
            val id = EntityType.getKey(entity.type)
            handleBreak(player as ServerPlayer, id.toString())
        }
    }

    private fun handleBreak(player: ServerPlayer, block: String): EventResult {
        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = BreakEvent(mod.khs, khsPlayer, block)
        onBreak(khsEvent)

        return eventResult(khsEvent)
    }

    private fun handlePlace(player: ServerPlayer, block: String): EventResult {
        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = PlaceEvent(mod.khs, khsPlayer, block)
        onPlace(khsEvent)

        return eventResult(khsEvent)
    }
}
