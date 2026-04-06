package cat.freya.khs.bukkit

import cat.freya.khs.world.Material
import cat.freya.khs.world.ResourceKey
import com.cryptomorin.xseries.XMaterial
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import kotlin.jvm.optionals.getOrNull

class BukkitMaterial(val inner: org.bukkit.Material) : Material {

    override val key: ResourceKey

    override val isBlock: Boolean = inner.isBlock

    override val isItem: Boolean = inner.isItem

    private fun parseMinecraftId(): Int? {
        if (XMaterial.supports(13)) return null

        @Suppress("DEPRECATION")
        return inner.getId()
    }

    private fun parseMinecraftKey(): String? {
        if (!XMaterial.supports(13)) return null

        if (isBlock) {
            val blockData = inner.createBlockData()
            return SpigotConversionUtil.fromBukkitBlockData(blockData).type.toString()
        } else {
            val type = SpigotConversionUtil.fromBukkitItemMaterial(inner)
            return type.name.toString()
        }
    }

    override fun toString(): String {
        return key.toString()
    }

    init {
        val minecraftKey = parseMinecraftKey()
        val minecraftId = parseMinecraftId()
        val platformKey = inner.name

        key = ResourceKey(minecraftKey, minecraftId, platformKey)
    }

    companion object {
        fun parse(name: String): BukkitMaterial? {
            val material = XMaterial.matchXMaterial(name).getOrNull() ?: return null
            val bukkitMaterial = material.get() ?: return null
            return BukkitMaterial(bukkitMaterial)
        }
    }
}
