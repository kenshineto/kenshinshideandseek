package cat.freya.khs.fabric.event

import cat.freya.khs.event.DamageEvent
import cat.freya.khs.event.onDamage
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

class DamageListener(val mod: KhsMod) {
    init {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register { entity, source, amount ->
            handleDamage(entity, source.entity, amount)
        }
    }

    private fun handleDamage(entity: Entity, attackerEntity: Entity?, amount: Float): Boolean {
        val player = entity as? ServerPlayer ?: return true
        val attacker = attackerEntity as? ServerPlayer

        val khsPlayer = ModPlayer(mod, player)
        val khsAttacker = attacker?.let { ModPlayer(mod, it) }
        val khsEvent = DamageEvent(mod.khs, khsPlayer, khsAttacker, amount.toDouble())
        onDamage(khsEvent)

        return !khsEvent.cancelled
    }
}
