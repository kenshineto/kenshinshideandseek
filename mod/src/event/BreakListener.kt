package cat.freya.khs.mod.event

import cat.freya.khs.event.BreakEvent
import cat.freya.khs.event.onBreak
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.BlockEvent
import dev.architectury.event.events.common.InteractionEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType

class BreakListener(val mod: KhsMod) {
    init {
        BlockEvent.BREAK.register { _, _, state, player, _ ->
            handleBreak(player as ServerPlayer, state.block.name.string)
        }

        InteractionEvent.INTERACT_ENTITY.register { player, entity, _ ->
            val type =
                runCatching { EntityType.getKey(entity.type) }.getOrDefault(null)
                    ?: return@register EventResult.pass()

            handleBreak(player as ServerPlayer, type.toString())
        }
    }

    private fun handleBreak(player: ServerPlayer, block: String): EventResult {
        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = BreakEvent(mod.khs, khsPlayer, block)
        onBreak(khsEvent)

        return eventResult(khsEvent)
    }
}
