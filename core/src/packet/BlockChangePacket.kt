package cat.freya.khs.packet

import cat.freya.khs.type.Material
import cat.freya.khs.world.Location
import cat.freya.khs.world.Player
import com.github.retrooper.packetevents.util.Vector3i
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange
import kotlin.math.floor

data class BlockChangePacket(val location: Location, val material: Material) : Packet {
    override fun send(player: Player) {
        val blockId = material.key.toBlockId() ?: return
        val blockX = floor(location.x).toInt()
        val blockY = floor(location.y).toInt()
        val blockZ = floor(location.z).toInt()
        val vector = Vector3i(blockX, blockY, blockZ)
        val packet = WrapperPlayServerBlockChange(vector, blockId)
        player.sendPacket(packet)
    }
}
