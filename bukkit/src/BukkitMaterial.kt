package cat.freya.khs.bukkit

import cat.freya.khs.type.Material
import cat.freya.khs.type.ResourceKey
import com.cryptomorin.xseries.XMaterial
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import kotlin.jvm.optionals.getOrNull

class BukkitMaterial(val inner: org.bukkit.Material) : Material {

    override val key: ResourceKey = getResourceKey()

    override val isBlock: Boolean = inner.isBlock

    override val isItem: Boolean = inner.isItem

    private fun getResourceKey(): ResourceKey {
        val minecraftKey = getMinecraftKey()
        val minecraftId = getMinecraftId()
        val platformKey = inner.name
        return ResourceKey(minecraftKey, minecraftId, platformKey)
    }

    private fun getMinecraftKey(): String? {
        if (!XMaterial.supports(1, 13)) return null

        if (isBlock) {
            val blockData = inner.createBlockData()
            return SpigotConversionUtil.fromBukkitBlockData(blockData).type.toString()
        } else {
            val type = SpigotConversionUtil.fromBukkitItemMaterial(inner)
            return type.name.toString()
        }
    }

    @Suppress("DEPRECATION")
    private fun getMinecraftId(): UInt? {
        if (XMaterial.supports(1, 13)) return null

        val id = inner.id
        if (id < 0) return null
        return id.toUInt()
    }

    override fun toString(): String {
        return key.toString()
    }

    companion object {
        fun parse(name: String): BukkitMaterial? {
            val material = XMaterial.matchXMaterial(name).getOrNull() ?: return null
            val bukkitMaterial = material.get() ?: return null
            return BukkitMaterial(bukkitMaterial)
        }
    }
}
