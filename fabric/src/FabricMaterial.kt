package cat.freya.khs.fabric

import cat.freya.khs.type.Material
import cat.freya.khs.type.ResourceKey
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.resources.ResourceKey as McResourceKey

class FabricBlockMaterial(val block: Holder<Block>, key: McResourceKey<Block>) : FabricMaterial(key) {
    override val isItem = false
    override val isBlock = true
}

class FabricItemMaterial(val item: Holder<Item>, key: McResourceKey<Item>) : FabricMaterial(key) {
    override val isItem = true
    override val isBlock = false
}

abstract class FabricMaterial(val inner: McResourceKey<*>) : Material {
    private val name = inner.identifier().toString()

    override val key: ResourceKey = ResourceKey(name, null, name)

    companion object {
        fun parse(name: String): FabricMaterial? {
            val id = Identifier.parse(name) ?: return null

            val block: Holder<Block>? = BuiltInRegistries.BLOCK.get(id).orElse(null)
            if (block != null) {
                val key = McResourceKey.create(Registries.BLOCK, id)
                return FabricBlockMaterial(block, key)
            }

            val item: Holder<Item>? = BuiltInRegistries.ITEM.get(id).orElse(null)
            if (item != null) {
                val key = McResourceKey.create(Registries.ITEM, id)
                return FabricItemMaterial(item, key)
            }

            return null
        }
    }
}
