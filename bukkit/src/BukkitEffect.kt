package cat.freya.khs.bukkit

import cat.freya.khs.config.EffectConfig
import cat.freya.khs.type.Effect
import cat.freya.khs.type.ResourceKey
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class BukkitEffect(val inner: PotionEffect, override val config: EffectConfig) : Effect {
    @Suppress("DEPRECATION")
    override val name = inner.type.name
    override val key: ResourceKey = getResourceKey()

    private fun getResourceKey(): ResourceKey {
        val minecraftKey = getMinecraftKey()
        val minecraftId = getMinecraftId()
        val platformKey = name
        return ResourceKey(minecraftKey, minecraftId, platformKey)
    }

    private fun getMinecraftKey(): String? {
        val type = SpigotConversionUtil.fromBukkitPotionEffectType(inner.type)
        return runCatching { type.name.toString() }.getOrElse { null }
    }

    private fun getMinecraftId(): UInt? {
        val type = SpigotConversionUtil.fromBukkitPotionEffectType(inner.type)
        return runCatching {
            val id = type.getId(null)
            if (id < 0) error("invalid id")
            id.toUInt()
        }.getOrElse { null }
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
