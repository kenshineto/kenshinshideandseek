package cat.freya.khs.mod.event

import cat.freya.khs.event.Event
import dev.architectury.event.EventResult

fun eventResult(event: Event): EventResult {
    return if (event.cancelled) EventResult.interruptFalse() else EventResult.pass()
}
