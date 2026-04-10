package cat.freya.khs.type

import cat.freya.khs.config.ItemConfig

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
