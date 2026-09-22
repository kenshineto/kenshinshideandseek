package cat.freya.khs.type

import cat.freya.khs.config.ItemConfig

interface Item {
    val name: String?
    val config: ItemConfig?

    // the internal string used to represent
    // this item type in the platform (not minecraft)
    val platformType: String

    fun similar(config: ItemConfig): Boolean {
        return this.config == config
    }

    fun similar(platformType: String): Boolean {
        return this.platformType == platformType
    }
}
