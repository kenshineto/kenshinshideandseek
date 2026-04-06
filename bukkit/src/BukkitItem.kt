package cat.freya.khs.bukkit

import cat.freya.khs.config.EffectConfig
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.world.Effect
import cat.freya.khs.world.Item
import cat.freya.khs.world.ResourceKey
import com.cryptomorin.xseries.XItemStack
import com.cryptomorin.xseries.XMaterial
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import kotlin.collections.emptyMap
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class BukkitItem(val inner: ItemStack, override val config: ItemConfig) : Item {
    override val name = inner.itemMeta?.displayName
    override val material = BukkitMaterial(inner.type)

    override fun similar(config: ItemConfig): Boolean {
        val item = BukkitItem.parse(config) ?: return false
        return inner.isSimilar(item.inner)
    }

    override fun similar(type: String): Boolean {
        val material = BukkitMaterial.parse(type) ?: return false
        return material.key.platformKey == material.key.platformKey
    }

    companion object {
        fun parse(itemConfig: ItemConfig): BukkitItem? {
            val config = YamlConfiguration()
            val materialParts = itemConfig.material.uppercase().split(":")
            val material = materialParts.first()

            // set name and material
            config.set("name", itemConfig.name?.let { formatText(it) })
            config.set("material", material)
            if (!itemConfig.lore.isEmpty())
                config.set("lore", itemConfig.lore.map { formatText(it) })
            config.set("unbreakable", itemConfig.unbreakable ?: false)

            // parse enchantments
            val enchantments = YamlConfiguration().createSection("enchants")
            for ((enchantment, value) in itemConfig.enchantments) enchantments.set(
                enchantment,
                value.toInt(),
            )
            config.set("enchants", enchantments)

            // set custom model data (1.14+)
            if (itemConfig.modelData != null)
                config.set("model-data", itemConfig.modelData?.toInt())

            // TODO: potions are broken on 1.8

            // set potion data
            if (material.endsWith("POTION")) {
                val potionType = materialParts.getOrNull(1) ?: "AWKWARD"
                config.set("base-type", potionType)
            }

            val item = runCatching {
                val item = (XItemStack.Deserializer()).withConfig(config).read() ?: return null

                // set player head owner (if skull)
                if (itemConfig.owner != null && itemConfig.material == "PLAYER_HEAD") {
                    val meta = item.itemMeta as SkullMeta
                    @Suppress("DEPRECATION")
                    meta.owner = itemConfig.owner
                    item.itemMeta = meta
                }

                BukkitItem(item, itemConfig)
            }

            return item.getOrDefault(null)
        }

        fun wrap(inner: ItemStack?): BukkitItem? {
            if (inner == null) return null

            val bukkitConfig = MemoryConfiguration()
            XItemStack.Serializer().withItem(inner).withConfig(bukkitConfig).write()

            val config = ItemConfig()
            config.name = bukkitConfig.getString("name")
            config.material = bukkitConfig.getString("material") ?: "NONE"
            config.lore = bukkitConfig.getStringList("lore")
            config.unbreakable = bukkitConfig.getBoolean("unbreakable", false)

            // read enchants
            config.enchantments =
                bukkitConfig.getConfigurationSection("enchants")?.let { map ->
                    map.getKeys(false).associateWith { map.getInt(it).toUInt() }
                } ?: emptyMap()

            return BukkitItem(inner, config)
        }
    }
}

class BukkitEffect(val inner: PotionEffect, override val config: EffectConfig) : Effect {
    @Suppress("DEPRECATION") override val name = inner.type.name
    override val key: ResourceKey

    init {
        val platformKey = name
        val type = SpigotConversionUtil.fromBukkitPotionEffectType(inner.type)
        val (minecraftKey, minecraftId) =
            if (XMaterial.supports(13)) {
                type.name.toString() to null
            } else {
                null to type.getId(null)
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
