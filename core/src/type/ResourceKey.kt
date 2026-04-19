package cat.freya.khs.type

import cat.freya.khs.world.Player
import com.github.retrooper.packetevents.protocol.entity.type.EntityType
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes

/**
 * Represents a key name for a block/item/potion effect Stores both the native minecraft and
 * platform names
 */
data class ResourceKey(
    /** The namespaced key used 1.13+ */
    val minecraftKey: String?,
    /** The id used pre 1.13 */
    val minecraftId: UInt?,
    /**
     * A custom identifier used within the platform. On some platforms, this will be the same as
     * [minecraftKey]
     */
    val platformKey: String,
) {
    // this is properly cached since the
    // material it comes from is also cached
    private var cachedBlockId: Int? = null

    fun toEntityType(client: Player): EntityType? {
        val clientVersion = client.getClientVersion()

        if (minecraftId != null) {
            val type = EntityTypes.getByLegacyId(clientVersion, minecraftId.toInt())
            if (type != null) return type
        }

        if (minecraftKey != null) {
            val type = EntityTypes.getByName(minecraftKey)
            if (type != null) return type
        }

        return null
    }

    private fun computeBlockId(): Int? {
        if (minecraftId != null) {
            return (minecraftId shl 4).toInt()
        }

        if (minecraftKey != null) {
            val state = StateTypes.getByName(minecraftKey)?.createBlockState()
            if (state != null) return state.globalId
        }

        return null
    }

    fun toBlockId(): Int? {
        val id = cachedBlockId ?: computeBlockId()
        cachedBlockId = id
        return id
    }

    override fun toString(): String {
        return minecraftKey ?: minecraftId?.toString() ?: platformKey
    }
}
