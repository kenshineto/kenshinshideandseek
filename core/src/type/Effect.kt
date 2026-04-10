package cat.freya.khs.type

import cat.freya.khs.config.EffectConfig

interface Effect {
    /** The name of the potion effect */
    val name: String?

    /** What type of the effect */
    val key: ResourceKey

    /** The config used to generate this effect */
    val config: EffectConfig?
}
