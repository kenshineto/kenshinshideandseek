package cat.freya.khs.packet

import cat.freya.khs.world.Location
import cat.freya.khs.world.Material
import cat.freya.khs.world.Player
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes
import com.github.retrooper.packetevents.util.Vector3i
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange
import kotlin.math.floor

data class BlockChangePacket(val location: Location, val material: Material) {

    private val packet: WrapperPlayServerBlockChange? = getPacket()

    private fun getPacket(): WrapperPlayServerBlockChange? {
        val vector =
            Vector3i(
                floor(location.x).toInt(),
                floor(location.y).toInt(),
                floor(location.z).toInt(),
            )

        val key = material.key

        return when {
            key.minecraftKey != null -> {
                val state =
                    StateTypes.getByName(key.minecraftKey)?.createBlockState() ?: return null
                WrapperPlayServerBlockChange(vector, state)
            }
            key.minecraftId != null -> {
                val id = key.minecraftId shl 4
                WrapperPlayServerBlockChange(vector, id)
            }
            else -> return null
        }
    }

    fun send(player: Player) {
        val packet = packet ?: return
        player.sendPacket(packet)
    }
}
