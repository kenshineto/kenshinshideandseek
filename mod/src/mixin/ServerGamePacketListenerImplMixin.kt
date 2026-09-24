package cat.freya.khs.mod.mixin

import cat.freya.khs.event.CommandEvent
import cat.freya.khs.event.DropEvent
import cat.freya.khs.event.KickEvent
import cat.freya.khs.event.SwingEvent
import cat.freya.khs.event.onCommand
import cat.freya.khs.event.onDrop
import cat.freya.khs.event.onKick
import cat.freya.khs.event.onSwing
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModItem
import cat.freya.khs.mod.ModPlayer
import net.minecraft.network.DisconnectionDetails
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action
import net.minecraft.network.protocol.game.ServerboundPunchPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ServerGamePacketListenerImpl::class)
abstract class ServerGamePacketListenerImplMixin {

    @Shadow lateinit var player: ServerPlayer

    @Suppress("UNUSED_PARAMETER")
    @Inject(method = ["handlePunch"], at = [At("HEAD")])
    private fun onPlayerSwing(packet: ServerboundPunchPacket, ci: CallbackInfo) {
        val mod = KhsMod.INSTANCE ?: return

        val khsPlayer = ModPlayer(mod, player)
        val event = SwingEvent(mod.khs, khsPlayer)
        onSwing(event)
    }

    @Inject(method = ["handleChatCommand"], at = [At("HEAD")], cancellable = true)
    private fun onChatCommand(packet: ServerboundChatCommandPacket, ci: CallbackInfo) {
        val mod = KhsMod.INSTANCE ?: return

        val khsPlayer = ModPlayer(mod, player)
        val event = CommandEvent(mod.khs, khsPlayer, packet.command())
        onCommand(event)

        if (event.cancelled) {
            ci.cancel()
        }
    }

    @Inject(method = ["onDisconnect"], at = [At("HEAD")], cancellable = true)
    private fun onPlayerDisconnect(details: DisconnectionDetails, ci: CallbackInfo) {
        val mod = KhsMod.INSTANCE ?: return

        val khsPlayer = ModPlayer(mod, player)
        val event = KickEvent(mod.khs, khsPlayer, details.reason().string)
        onKick(event)

        if (event.cancelled) {
            ci.cancel()
        }
    }

    @Inject(method = ["handlePlayerAction"], at = [At("HEAD")], cancellable = true)
    private fun onPlayerAction(packet: ServerboundPlayerActionPacket, ci: CallbackInfo) {
        val mod = KhsMod.INSTANCE ?: return

        // ignore other actions
        if (packet.action != Action.DROP_ITEM && packet.action != Action.DROP_ALL_ITEMS) return

        val item = ModItem.wrap(player.inventory.selectedItem) ?: return
        val khsPlayer = ModPlayer(mod, player)
        val event = DropEvent(mod.khs, khsPlayer, item)
        onDrop(event)

        if (event.cancelled) {
            player.inventoryMenu.sendAllDataToRemote()
            ci.cancel()
        }
    }
}
