package cat.freya.khs.fabric.event

import cat.freya.khs.event.InteractEvent
import cat.freya.khs.event.UseEvent
import cat.freya.khs.event.onInteract
import cat.freya.khs.event.onUse
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModItem
import cat.freya.khs.mod.ModPlayer
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult

class InteractListener(val mod: KhsMod) {
    init {
        UseBlockCallback.EVENT.register { player, level, _, hitResult ->
            val state = level.getBlockState(hitResult.blockPos)
            val id = BuiltInRegistries.BLOCK.getKey(state.block)
            handleInteract(player as ServerPlayer, id)
        }

        UseEntityCallback.EVENT.register { player, _, _, entity, _ ->
            val id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)
            handleInteract(player as ServerPlayer, id)
        }

        UseItemCallback.EVENT.register { player, level, hand ->
            handleUse(player as ServerPlayer, hand)
        }
    }

    private fun handleInteract(player: ServerPlayer, id: Identifier): InteractionResult {
        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = InteractEvent(mod.khs, khsPlayer, id.toString())
        onInteract(khsEvent)

        return eventResult(khsEvent)
    }

    private fun handleUse(player: ServerPlayer, hand: InteractionHand): InteractionResult {
        val item = player.getItemInHand(hand)
        val khsPlayer = ModPlayer(mod, player)
        val khsItem = ModItem.wrap(item) ?: return InteractionResult.PASS
        val khsEvent = UseEvent(mod.khs, khsPlayer, khsItem)
        onUse(khsEvent)

        return eventResult(khsEvent)
    }
}
