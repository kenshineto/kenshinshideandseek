package cat.freya.khs.fabric.event

import cat.freya.khs.event.MoveEvent
import cat.freya.khs.event.onMove
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import cat.freya.khs.world.Location
import java.util.UUID
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

class MovementListener(val mod: KhsMod) {
    private val savedPrevLocation: MutableMap<UUID, Location> = mutableMapOf()

    init {
        ServerTickEvents.END_SERVER_TICK.register { _ ->
            mod.server.getPlayers().forEach { handlePlayer(it) }
        }
    }

    private fun handlePlayer(player: ModPlayer) {
        val to = player.getLocation()
        val from = savedPrevLocation[player.uuid] ?: to

        if (from.worldName == to.worldName && from.toPosition() != to.toPosition()) {
            val khsEvent = MoveEvent(mod.khs, player, from.toPosition(), to.toPosition())
            onMove(khsEvent)

            if (khsEvent.cancelled) {
                player.teleport(from)
            }
        }

        savedPrevLocation[player.uuid] = to
    }
}
