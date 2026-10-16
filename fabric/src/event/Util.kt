package cat.freya.khs.fabric.event

import cat.freya.khs.event.Event
import net.minecraft.world.InteractionResult

fun eventResult(event: Event): InteractionResult {
    return if (event.cancelled) InteractionResult.FAIL else InteractionResult.PASS
}
