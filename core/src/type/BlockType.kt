package cat.freya.khs.type

import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes

class BlockType(val mcType: String, val platformType: String) {
    fun getBlockId(): Int? {
        val id = mcType.toIntOrNull()
        if (id != null) {
            // global id's for packets are shifted
            // left by 4 from the block id
            return id shl 4
        }

        // parse 1.13+ resource key
        return StateTypes.getByName(mcType)?.createBlockState()?.globalId
    }
}
