package cat.freya.khs.packet

import cat.freya.khs.world.Player
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.PacketWrapper

interface Packet {
    @Throws(Throwable::class) fun create(player: Player): PacketWrapper<*>?

    fun send(player: Player) {
        runCatching {
                val packet = create(player) ?: return
                val handle = player.getHandle()
                PacketEvents.getAPI().playerManager.sendPacket(handle, packet)
            }
            .onFailure {
                // TODO: where to log error message?
            }
    }
}
