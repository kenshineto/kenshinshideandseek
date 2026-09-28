package cat.freya.khs

import cat.freya.khs.world.Player
import java.util.UUID

class RequestManager(private val plugin: Khs) {
    private val requests: MutableMap<UUID, Request> = mutableMapOf()

    fun request(player: Player, delayTicks: ULong, fn: () -> Unit) {
        synchronized(requests) {
            requests[player.uuid] = Request(delayTicks, fn)
        }

        val seconds = delayTicks.toDouble() / 20.0
        player.message(plugin.locale.prefix.default + plugin.locale.confirm.confirm.with(seconds))
    }

    fun update() {
        synchronized(requests) {
            requests.forEach { entry ->
                val uuid = entry.key
                val req = entry.value
                req.update()

                if (req.isExpired()) {
                    requests.remove(uuid)

                    val player = plugin.shim.getPlayer(uuid)
                    if (player != null) {
                        player.message(plugin.locale.prefix.error + plugin.locale.confirm.timedOut)
                    }
                }
            }
        }
    }

    fun execute(player: Player) {
        synchronized(requests) {
            val request = requests.remove(player.uuid)
            if (request == null) {
                player.message(plugin.locale.prefix.error + plugin.locale.confirm.none)
            } else {
                request.execute()
            }
        }
    }
}
