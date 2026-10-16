package cat.freya.khs.fabric.event

import cat.freya.khs.event.ChatEvent
import cat.freya.khs.event.CommandEvent
import cat.freya.khs.event.onChat
import cat.freya.khs.event.onCommand
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.minecraft.server.level.ServerPlayer

class ChatListener(val mod: KhsMod) {
    init {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register { message, player, _ ->
            handleChat(player, message.signedContent())
        }

        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register { message, source, _ ->
            val player = source.player ?: return@register true
            handleCommand(player, message.signedContent())
        }
    }

    private fun handleChat(player: ServerPlayer, message: String): Boolean {
        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = ChatEvent(mod.khs, khsPlayer, message)
        onChat(khsEvent)

        return !khsEvent.cancelled
    }

    private fun handleCommand(player: ServerPlayer, message: String): Boolean {
        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = CommandEvent(mod.khs, khsPlayer, message)
        onCommand(khsEvent)

        return !khsEvent.cancelled
    }
}
