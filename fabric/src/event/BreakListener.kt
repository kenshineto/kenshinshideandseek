package cat.freya.khs.fabric.event

import cat.freya.khs.event.BreakEvent
import cat.freya.khs.event.PlaceEvent
import cat.freya.khs.event.onBreak
import cat.freya.khs.event.onPlace
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult

class BreakListener(val mod: KhsMod) {
    init {
        PlayerBlockBreakEvents.BEFORE.register { _, player, _, state, _ ->
            val id = BuiltInRegistries.BLOCK.getKey(state.block)
            handleBreak(player as ServerPlayer, id.toString())
        }

        // BlockEvent.PLACE.register { _, _, state, player ->
        //    val id = BuiltInRegistries.BLOCK.getKey(state.block)
        //    handlePlace(player as ServerPlayer, id.toString())
        // }
    }

    private fun handleBreak(player: ServerPlayer, block: String): Boolean {
        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = BreakEvent(mod.khs, khsPlayer, block)
        onBreak(khsEvent)

        return !khsEvent.cancelled
    }

    private fun handlePlace(player: ServerPlayer, block: String): InteractionResult {
        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = PlaceEvent(mod.khs, khsPlayer, block)
        onPlace(khsEvent)

        return eventResult(khsEvent)
    }
}
