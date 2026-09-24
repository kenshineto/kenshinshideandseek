package cat.freya.khs.type

import cat.freya.khs.config.EffectConfig

interface Effect {
    val name: String?
    val config: EffectConfig?

    // the internal string used to represent
    // this effect type in the platform (not minecraft)
    val platformType: String

    fun similar(config: EffectConfig): Boolean {
        return this.config == config
    }

    fun similar(platformType: String): Boolean {
        return this.platformType == platformType
    }

    override fun toString(): String
}
