package cat.freya.khs.mod.event

import cat.freya.khs.event.DamageEvent
import cat.freya.khs.event.onDamage
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.EntityEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

class DamageListener(val mod: KhsMod) {
    init {
        EntityEvent.LIVING_HURT.register { entity, source, amount ->
            handleDamage(entity, source.entity, amount)
        }
    }

    private fun handleDamage(entity: Entity, attackerEntity: Entity?, amount: Float): EventResult {
        val player = entity as? ServerPlayer ?: return EventResult.pass()
        val attacker = attackerEntity as? ServerPlayer

        val khsPlayer = ModPlayer(mod, player)
        val khsAttacker = attacker?.let { ModPlayer(mod, it) }
        val khsEvent = DamageEvent(mod.khs, khsPlayer, khsAttacker, amount.toDouble())
        onDamage(khsEvent)

        return eventResult(khsEvent)
    }
}
