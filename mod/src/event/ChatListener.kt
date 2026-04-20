package cat.freya.khs.mod.event

import cat.freya.khs.event.ChatEvent
import cat.freya.khs.event.onChat
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import dev.architectury.event.EventResult
import net.minecraft.server.level.ServerPlayer

class ChatListener(val mod: KhsMod) {
    init {
        dev.architectury.event.events.common.ChatEvent.RECEIVED.register { player, message ->
            handleChat(player as ServerPlayer, message.string)
        }
    }

    private fun handleChat(player: ServerPlayer, message: String): EventResult {
        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = ChatEvent(mod.khs, khsPlayer, message)
        onChat(khsEvent)

        return eventResult(khsEvent)
    }
}
