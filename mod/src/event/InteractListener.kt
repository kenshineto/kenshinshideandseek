package cat.freya.khs.mod.event

import cat.freya.khs.event.InteractEvent
import cat.freya.khs.event.UseEvent
import cat.freya.khs.event.onInteract
import cat.freya.khs.event.onUse
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModItem
import cat.freya.khs.mod.ModPlayer
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.InteractionEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand

class InteractListener(val mod: KhsMod) {
    init {
        InteractionEvent.LEFT_CLICK_BLOCK.register { player, _, pos, _ ->
            handleInteract(player as ServerPlayer, pos)
        }

        InteractionEvent.RIGHT_CLICK_BLOCK.register { player, _, pos, _ ->
            handleInteract(player as ServerPlayer, pos)
        }

        InteractionEvent.INTERACT_ENTITY.register { player, _, _ ->
            handleInteract(player as ServerPlayer, null)
        }

        InteractionEvent.RIGHT_CLICK_ITEM.register { player, hand ->
            handleUse(player as ServerPlayer, hand)
        }
    }

    private fun handleInteract(player: ServerPlayer, pos: BlockPos?): EventResult {
        val block = pos?.let { player.level().getBlockState(it) }

        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = InteractEvent(mod.khs, khsPlayer, block?.block?.name?.string)
        onInteract(khsEvent)

        return if (khsEvent.cancelled) EventResult.interruptFalse() else EventResult.pass()
    }

    private fun handleUse(player: ServerPlayer, hand: InteractionHand): EventResult {
        val item = player.getItemInHand(hand)

        val khsPlayer = ModPlayer(mod, player)
        val khsItem = ModItem.wrap(item) ?: return EventResult.pass()
        val khsEvent = UseEvent(mod.khs, khsPlayer, khsItem)
        onUse(khsEvent)

        return if (khsEvent.cancelled) EventResult.interruptFalse() else EventResult.pass()
    }
}
