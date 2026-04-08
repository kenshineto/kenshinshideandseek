package cat.freya.khs.world

import cat.freya.khs.config.EffectConfig
import cat.freya.khs.config.ItemConfig

/**
 * Represents a key name for a block/item/potiton effect Stores both the native minecraft and
 * platform names
 */
data class ResourceKey(
    /** The namespaced key used 1.13+ */
    val minecraftKey: String?,

    /** The id used pre 1.13 */
    val minecraftId: Int?,

    /**
     * A cutstom identifier used within the platform. On some platforms, this will be the same as
     * [minecraftKey]
     */
    val platformKey: String,
) {
    override fun toString(): String {
        return minecraftKey ?: minecraftId?.toString() ?: platformKey
    }
}

/**
 * Represents a material/type for a minecraft block/item It holds both the names for the minecraft
 * type, and the platform type
 */
interface Material {
    /** The platform/native mc name of this type */
    val key: ResourceKey

    /** If this material represents a block */
    val isBlock: Boolean

    /** If this material represents an item */
    val isItem: Boolean
}

/** Represents a stack of items */
interface Item {
    /** The name of the item */
    val name: String?

    /** The type of the item */
    val material: Material

    /** The config used to generate this item */
    val config: ItemConfig?

    /** If the item is similar to the config that generated it */
    fun similar(config: ItemConfig): Boolean {
        return this.config == config
    }

    fun similar(type: String): Boolean {
        return (this.material.key.platformKey == type) || (this.material.key.minecraftKey == type)
    }
}

interface Effect {
    /** The name of the potion effect */
    val name: String?

    /** What type of the effect */
    val key: ResourceKey

    /** The config used to generate this effect */
    val config: EffectConfig?
}
