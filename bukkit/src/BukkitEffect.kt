package cat.freya.khs.bukkit

import cat.freya.khs.config.EffectConfig
import cat.freya.khs.type.Effect
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class BukkitEffect(val inner: PotionEffect, override val config: EffectConfig) : Effect {
    @Suppress("DEPRECATION") override val name = inner.type.name
    override val platformType = inner.type.toString()

    companion object {
        fun parse(config: EffectConfig): BukkitEffect? {
            @Suppress("DEPRECATION") val type = PotionEffectType.getByName(config.type.uppercase()) ?: return null
            val inner =
                PotionEffect(
                    type,
                    config.duration.toInt(),
                    config.amplifier.toInt(),
                    config.ambient,
                    config.particles,
                )

            return BukkitEffect(inner, config)
        }
    }
}
