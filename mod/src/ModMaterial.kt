package cat.freya.khs.mod

import cat.freya.khs.type.Material
import cat.freya.khs.type.ResourceKey
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.resources.ResourceKey as McResourceKey

class ModBlockMaterial(val block: Holder<Block>, key: McResourceKey<Block>) : ModMaterial(key) {
    override val isItem = false
    override val isBlock = true
}

class ModItemMaterial(val item: Holder<Item>, key: McResourceKey<Item>) : ModMaterial(key) {
    override val isItem = true
    override val isBlock = false
}

abstract class ModMaterial(val inner: McResourceKey<*>) : Material {
    private val name = inner.identifier().toString()

    override val key: ResourceKey = ResourceKey(name, null, name)

    companion object {
        fun parse(name: String): ModMaterial? {
            val id = Identifier.parse(name)

            val block: Holder<Block>? = BuiltInRegistries.BLOCK.get(id).orElse(null)
            if (block != null) {
                val key = McResourceKey.create(Registries.BLOCK, id)
                return ModBlockMaterial(block, key)
            }

            val item: Holder<Item>? = BuiltInRegistries.ITEM.get(id).orElse(null)
            if (item != null) {
                val key = McResourceKey.create(Registries.ITEM, id)
                return ModItemMaterial(item, key)
            }

            return null
        }
    }
}
