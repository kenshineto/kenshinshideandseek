package cat.freya.khs.bukkit

import cat.freya.khs.config.EffectConfig
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.world.Effect as KhsEffect
import cat.freya.khs.world.Item as KhsItem
import cat.freya.khs.world.Material as KhsMaterial
import com.cryptomorin.xseries.XItemStack
import com.cryptomorin.xseries.XMaterial
import kotlin.collections.emptyMap
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.potion.PotionEffect

class BukkitKhsItem(val inner: ItemStack, override val config: ItemConfig) : KhsItem {
    override val name: String? = inner.itemMeta?.displayName
    override val material: KhsMaterial
        get() = KhsMaterial(null, inner.type.name)

    override fun clone(): KhsItem = BukkitKhsItem(inner, config)

    override fun similar(config: ItemConfig): Boolean {
        val item = parseBukkitItem(config) ?: return false
        return inner.isSimilar(item.inner)
    }

    override fun similar(material: String): Boolean {
        val xMaterial = XMaterial.matchXMaterial(material).orElse(null) ?: return false
        return xMaterial.isSimilar(inner)
    }
}

class BukkitKhsEffect(val inner: PotionEffect, override val config: EffectConfig) : KhsEffect {
    @Suppress("DEPRECATION") override val name = inner.type.name

    override fun clone(): KhsEffect = BukkitKhsEffect(inner, config)
}

fun parseBukkitItem(itemConfig: ItemConfig): BukkitKhsItem? {
    val config = YamlConfiguration().createSection("temp")
    val materialParts = itemConfig.material.uppercase().split(":")
    val material = materialParts.first()

    // set name and material
    config.set("name", itemConfig.name?.let { formatText(it) })
    config.set("material", material)
    if (!itemConfig.lore.isEmpty()) config.set("lore", itemConfig.lore.map { formatText(it) })
    config.set("unbreakable", itemConfig.unbreakable ?: false)

    // parse enchantments
    val enchantments = YamlConfiguration().createSection("enchantments")
    for ((enchantment, value) in itemConfig.enchantments) enchantments.set(
        enchantment,
        value.toInt(),
    )
    config.set("enchants", enchantments)

    // set custom model data (1.14+)
    if (itemConfig.modelData != null) config.set("model-data", itemConfig.modelData?.toInt())

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

        BukkitKhsItem(item, itemConfig)
    }

    return item.getOrDefault(null)
}

fun toKhsItem(inner: ItemStack?): BukkitKhsItem? {
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

    return BukkitKhsItem(inner, config)
}
