package cat.freya.khs.bukkit

import cat.freya.khs.config.EffectConfig
import cat.freya.khs.world.Effect
import cat.freya.khs.world.ResourceKey
import com.cryptomorin.xseries.XMaterial
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class BukkitEffect(val inner: PotionEffect, override val config: EffectConfig) : Effect {
    @Suppress("DEPRECATION") override val name = inner.type.name
    override val key: ResourceKey

    init {
        val platformKey = name
        val type = SpigotConversionUtil.fromBukkitPotionEffectType(inner.type)

        val minecraftId = runCatching { type.getId(null) }.getOrElse { null }
        val minecraftKey =
            if (XMaterial.supports(1, 13)) {
                type.name.toString()
            } else {
                null
            }

        key = ResourceKey(minecraftKey, minecraftId, platformKey)
    }

    companion object {
        fun parse(config: EffectConfig): BukkitEffect? {
            @Suppress("DEPRECATION")
            val type = PotionEffectType.getByName(config.type.uppercase()) ?: return null
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
