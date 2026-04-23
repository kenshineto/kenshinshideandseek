package cat.freya.khs.mod

import cat.freya.khs.config.ItemConfig
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.type.Item
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.util.Unit
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.component.ItemLore
import kotlin.collections.emptyMap

class ModItem(
    val inner: ItemStack,
    override val material: ModItemMaterial,
    override val config: ItemConfig,
) : Item {
    override val name: String = inner.displayName.string

    companion object {
        fun parse(server: ModServer, itemConfig: ItemConfig): ModItem? {
            val materialParts = itemConfig.material.split(":")
            val materialName = materialParts.first()

            val material = ModMaterial.parse(materialName) as? ModItemMaterial ?: return null
            val stack = ItemStack(material.item, 1)

            // name
            val name = itemConfig.name
            if (name != null) {
                stack.set(DataComponents.CUSTOM_NAME, KhsMod.parseText(name))
            }

            // lore
            val lore = itemConfig.lore
            stack.set(DataComponents.LORE, ItemLore(lore.map { KhsMod.parseText(it) }))

            // enchantments
            val enchants = ModEnchantment.parse(server, itemConfig.enchantments)
            stack.set(DataComponents.ENCHANTMENTS, enchants)

            // unbreakable
            if (itemConfig.unbreakable == true) {
                stack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE)
            }

            val potionName = materialParts.getOrNull(1)
            if (materialName.contains("potion", ignoreCase = true) && potionName != null) {
                val potionId: Identifier? = Identifier.tryParse(potionName)
                val potionType = potionId?.let { BuiltInRegistries.POTION.get(it) }?.orElse(null)

                if (potionType != null) {
                    val potion = PotionContents(potionType)
                    stack.set(DataComponents.POTION_CONTENTS, potion)
                }
            }

            // TODO: player head

            return ModItem(stack, material, itemConfig)
        }

        fun wrap(stack: ItemStack?): ModItem? {
            if (stack == null) return null

            val id = BuiltInRegistries.ITEM.getKey(stack.item)
            val key = ResourceKey.create(Registries.ITEM, id)
            val holder = BuiltInRegistries.ITEM.get(key).get()
            val material = ModItemMaterial(holder, key)

            val config = ItemConfig()
            config.name = stack.displayName.string
            config.material = id.toString()
            config.unbreakable = stack.get(DataComponents.UNBREAKABLE) != null

            config.lore = stack.get(DataComponents.LORE)?.lines()?.map { it.string } ?: emptyList()
            config.enchantments =
                stack.get(DataComponents.ENCHANTMENTS)?.entrySet()?.associate { (enchant, level) ->
                    enchant.registeredName to level.toUInt()
                } ?: emptyMap()

            return ModItem(stack, material, config)
        }
    }
}
