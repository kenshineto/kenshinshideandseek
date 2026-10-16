package cat.freya.khs.mod

import cat.freya.khs.Khs
import java.util.concurrent.atomic.AtomicBoolean
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.server.MinecraftServer

class KhsMod(val platform: String) {
    val enabled = AtomicBoolean(true)
    val server = ModServer(this)
    val shim = ModKhsShim(this)
    val khs = Khs(shim, modTypes)

    init {
        INSTANCE = this
    }

    fun init(inner: MinecraftServer) {
        server.init(inner)
        khs.init()

        // khs.init() can disable us
        if (!enabled.get()) return
    }

    fun onShutdown() {
        if (!enabled.getAndSet(false)) return
        khs.cleanup()
    }

    fun doTick() {
        if (!enabled.get()) return
        server.doTick()
        khs.doTick()
    }

    companion object {
        const val ID: String = "khs"

        var INSTANCE: KhsMod? = null
            private set

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
                val format = ChatFormatting.getByCode(code) ?: continue
                val asColor = TextColor.fromLegacyFormat(format)

                style =
                    when {
                        format == ChatFormatting.RESET -> Style.EMPTY
                        asColor != null -> Style.EMPTY.withColor(format)
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
