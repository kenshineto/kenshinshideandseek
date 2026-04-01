package cat.freya.khs.packet

import cat.freya.khs.player.Player
import cat.freya.khs.world.Location
import cat.freya.khs.world.Material
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes
import com.github.retrooper.packetevents.util.Vector3i
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange
import kotlin.math.floor

data class BlockChangePacket(val location: Location, val material: Material) {
    fun send(player: Player) {
        // 1.9+ only
        val materialName = material.mcName ?: return
        val state = StateTypes.getByName(materialName)?.createBlockState() ?: return
        val vector =
            Vector3i(
                floor(location.x).toInt(),
                floor(location.y).toInt(),
                floor(location.z).toInt(),
            )
        val packet = WrapperPlayServerBlockChange(vector, state)
        player.sendPacket(packet)
    }
}
