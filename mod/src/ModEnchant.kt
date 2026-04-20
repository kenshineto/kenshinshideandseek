package cat.freya.khs.mod

import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments

object ModEnchantment {
    fun parse(server: ModServer, name: String): Holder<Enchantment>? {
        val id = Identifier.tryParse(name) ?: return null
        val key = ResourceKey.create(Registries.ENCHANTMENT, id)

        val registry = server.inner.registryAccess()
        val enchant = registry.get(key).orElse(null) ?: return null

        return enchant
    }

    fun parse(server: ModServer, map: Map<String, UInt>): ItemEnchantments {
        val list = ItemEnchantments.Mutable(ItemEnchantments.EMPTY)

        for ((name, level) in map) {
            val enchant = ModEnchantment.parse(server, name) ?: continue
            list.set(enchant, level.toInt())
        }

        return list.toImmutable()
    }
}
