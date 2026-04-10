package cat.freya.khs.packet

import cat.freya.khs.world.Player
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.player.ClientVersion
import com.github.retrooper.packetevents.protocol.player.HumanoidArm
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientSettings

/** Represents the players client settings retrieved from the client_information packet */
data class ClientSettings(
    val skinMask: Byte = 0x74,
    val mainHand: HumanoidArm = HumanoidArm.RIGHT,
) {
    fun toData(client: Player): List<EntityData<*>> {
        val clientVersion = client.getClientVersion()
        val list = mutableListOf<EntityData<*>>()

        // i hate this
        // is there a better way of doing this?
        // likely not...
        if (clientVersion.isOlderThan(ClientVersion.V_1_16_4)) {
            list.add(EntityData(13, EntityDataTypes.BYTE, skinMask))
            list.add(EntityData(14, EntityDataTypes.BYTE, mainHand.id.toByte()))
        } else if (clientVersion.isOlderThan(ClientVersion.V_1_21_9)) {
            list.add(EntityData(17, EntityDataTypes.BYTE, skinMask))
            list.add(EntityData(18, EntityDataTypes.BYTE, mainHand.id.toByte()))
        } else {
            list.add(EntityData(16, EntityDataTypes.BYTE, skinMask))
            list.add(EntityData(15, EntityDataTypes.HUMANOID_ARM, mainHand))
        }

        return list
    }

    companion object {
        fun fromPacket(packet: WrapperCommonClientSettings<*>): ClientSettings {
            return ClientSettings(packet.getSkinMask(), packet.getMainHand())
        }
    }
}
