package cat.freya.khs.fabric.event

import cat.freya.khs.event.DeathEvent
import cat.freya.khs.event.RespawnEvent
import cat.freya.khs.event.onDeath
import cat.freya.khs.event.onRespawn
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

class RespawnListener(val mod: KhsMod) {
    init {
        ServerLivingEntityEvents.ALLOW_DEATH.register { entity, _, _ ->
            handleDeath(entity)
        }

        ServerEntityEvents.ENTITY_LOAD.register { entity, _ ->
            handleRespawn(entity)
        }
    }

    private fun handleDeath(entity: Entity): Boolean {
        val player = entity as? ServerPlayer ?: return true
        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = DeathEvent(mod.khs, khsPlayer)
        onDeath(khsEvent)

        return !khsEvent.cancelled
    }

    private fun handleRespawn(entity: Entity) {
        val player = entity as? ServerPlayer ?: return
        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = RespawnEvent(mod.khs, khsPlayer)
        onRespawn(khsEvent)
    }
}
