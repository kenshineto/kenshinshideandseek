package cat.freya.khs.mod

import cat.freya.khs.Khs
import cat.freya.khs.mod.event.*
import dev.architectury.event.events.common.CommandRegistrationEvent
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import java.util.concurrent.atomic.AtomicBoolean

class KhsMod(val info: Info) {
    data class Info(
        val pluginVersion: String,
        val platform: String,
    )

    val enabled = AtomicBoolean(true)
    val server = ModServer(this)
    val shim = ModKhsShim(this)
    val khs = Khs(shim)

    init {
        server.init()

        CommandRegistrationEvent.EVENT.register { dispatcher, _, _ ->
            ModCommand(this, khs.commandGroup, dispatcher)
        }
    }

    // called once the minecraft
    // server is starting
    fun init() {
        khs.init()

        // khs.init() can disable us
        if (!enabled.get()) return

        // TODO: register bungeecord

        registerListeners()
    }

    fun onShutdown() {
        if (!enabled.getAndSet(false)) return
        khs.cleanup()
    }

    fun onTick() {
        if (!enabled.get()) return
        khs.onTick()
    }

    private fun registerListeners() {
        BreakListener(this)
        ChatListener(this)
        // TODO: CommandListener(this)
        DamageListener(this)
        InteractListener(this)
        InventoryListener(this)
        JoinLeaveListener(this)
        MovementListener(this)
        PlayerListener(this)
        RespawnListener(this)
    }

    companion object {
        const val ID: String = "khs"

        fun parseText(input: String): Component {
            var result = Component.empty()
            var style = Style.EMPTY
            var i = 0

            val buffer = StringBuilder()

            while (i < input.length) {
                val c = input[i++]

                if (c != '&') {
                    buffer.append(c)
                    continue
                }

                if (buffer.isNotEmpty()) {
                    val content = Component.literal(buffer.toString()).setStyle(style)
                    result = result.append(content)
                    buffer.clear()
                }

                val code = input[i++].lowercaseChar()
                val format = ChatFormatting.getByCode(code)

                if (format == null) {
                    continue
                }

                style =
                    when {
                        format == ChatFormatting.RESET -> Style.EMPTY
                        format.isColor -> Style.EMPTY.withColor(format)
                        else -> style.applyFormat(format)
                    }
            }

            if (buffer.isNotEmpty()) {
                val content = Component.literal(buffer.toString()).setStyle(style)
                result = result.append(content)
            }

            return result
        }
    }
}
