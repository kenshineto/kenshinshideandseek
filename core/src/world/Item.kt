package cat.freya.khs.world

import cat.freya.khs.config.EffectConfig
import cat.freya.khs.config.ItemConfig

interface Item {
    val name: String?
    val material: String
    val config: ItemConfig

    fun clone(): Item

    fun similar(config: ItemConfig): Boolean

    fun similar(material: String): Boolean
}

interface Effect {
    val name: String?
    val config: EffectConfig

    fun clone(): Effect
}
