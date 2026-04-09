package cat.freya.khs.packet

import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.player.HumanoidArm
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientSettings

/** Represents the players client settings retrieved from the client_information packet */
data class ClientSettings(
    val skinMask: Byte = 0x74,
    val mainHand: HumanoidArm = HumanoidArm.RIGHT,
) {
    fun toData(): List<EntityData<*>> {
        val skinMaskData = EntityData(16, EntityDataTypes.BYTE, skinMask)
        val mainHandData = EntityData(15, EntityDataTypes.HUMANOID_ARM, mainHand)
        return listOf(skinMaskData, mainHandData)
    }

    companion object {
        fun fromPacket(packet: WrapperCommonClientSettings<*>): ClientSettings {
            return ClientSettings(packet.getSkinMask(), packet.getMainHand())
        }
    }
}
