package cat.freya.khs.event

import cat.freya.khs.Khs
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.text.buildString

abstract class Event(private val plugin: Khs) {
    var cancelled: Boolean = false

    fun cancel() {
        cancelled = true
    }

    fun debug() {
        if (!plugin.config.debug) {
            return
        }

        val eventName = this::class.simpleName
        val propValues =
            this::class
                .primaryConstructor!!
                .parameters
                .mapNotNull { param -> this::class.memberProperties.find { it.name == param.name } }
                .associateWith { prop -> prop.getter.call(this) }

        val message = buildString {
            append("${eventName}: ")
            propValues.entries.forEachIndexed { index, (prop, value) ->
                if (index != 0) {
                    append(", ")
                }
                append("${prop.name}=${value}")
            }
        }

        plugin.shim.logger.debug(message)
    }
}
