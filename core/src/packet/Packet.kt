package cat.freya.khs.packet

import cat.freya.khs.world.Player

interface Packet {
    fun send(player: Player)
}
