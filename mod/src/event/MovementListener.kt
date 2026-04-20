package cat.freya.khs.mod.event

import cat.freya.khs.event.MoveEvent
import cat.freya.khs.event.onMove
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlayer
import cat.freya.khs.world.Location
import dev.architectury.event.events.common.TickEvent
import java.util.UUID

class MovementListener(val mod: KhsMod) {
    private val savedPrevLocation: MutableMap<UUID, Location> = mutableMapOf()

    init {
        TickEvent.SERVER_POST.register { _ ->
            mod.server.getPlayers().forEach { handlePlayer(it) }
        }
    }

    private fun handlePlayer(player: ModPlayer) {
        val to = player.getLocation()
        val from = savedPrevLocation[player.uuid] ?: to

        if (from.worldName == to.worldName) {
            val khsEvent = MoveEvent(mod.khs, player, from.toPosition(), to.toPosition())
            onMove(khsEvent)

            if (khsEvent.cancelled) {
                player.teleport(from)
            }
        }

        savedPrevLocation[player.uuid] = to
    }
}
