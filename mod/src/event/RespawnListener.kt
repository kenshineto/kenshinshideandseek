package cat.freya.khs.mod.event

import cat.freya.khs.event.DeathEvent
import cat.freya.khs.event.RespawnEvent
import cat.freya.khs.event.onDeath
import cat.freya.khs.event.onRespawn
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.EntityEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

class RespawnListener(val mod: KhsMod) {
    init {
        EntityEvent.LIVING_DEATH.register { entity, _ ->
            handleDeath(entity)
        }

        EntityEvent.ADD.register { entity, _ ->
            handleRespawn(entity)
        }
    }

    private fun handleDeath(entity: Entity): EventResult {
        val player = entity as? ServerPlayer ?: return EventResult.pass()

        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = DeathEvent(mod.khs, khsPlayer)
        onDeath(khsEvent)

        return eventResult(khsEvent)
    }

    private fun handleRespawn(entity: Entity): EventResult {
        val player = entity as? ServerPlayer ?: return EventResult.pass()

        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = RespawnEvent(mod.khs, khsPlayer)
        onRespawn(khsEvent)

        return eventResult(khsEvent)
    }
}
