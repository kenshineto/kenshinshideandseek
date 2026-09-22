package cat.freya.khs.mod

import cat.freya.khs.config.ItemConfig
import cat.freya.khs.type.Item
import kotlin.collections.emptyMap
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.util.Unit
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.component.ItemLore

class ModItem(
    val inner: ItemStack,
    override val config: ItemConfig,
) : Item {
    override val name: String = inner.displayName.string

    private val mcType = BuiltInRegistries.ITEM.getId(inner.item).toString()
    override val platformType = mcType

    companion object {
        fun parse(server: ModServer, itemConfig: ItemConfig): ModItem? {
            val id = Identifier.tryParse(itemConfig.material) ?: return null
            val item = BuiltInRegistries.ITEM.get(id).orElse(null) ?: return null
            val stack = ItemStack(item, 1)

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

            val potionId = itemConfig.effect?.let(Identifier::tryParse)
            if (itemConfig.material.contains("potion", ignoreCase = true) && potionId != null) {
                val potionType = BuiltInRegistries.POTION.get(potionId).orElse(null)
                if (potionType != null) {
                    val potion = PotionContents(potionType)
                    stack.set(DataComponents.POTION_CONTENTS, potion)
                }
            }

            // TODO: player head

            return ModItem(stack, itemConfig)
        }

        fun wrap(stack: ItemStack?): ModItem? {
            if (stack == null) return null

            val id = BuiltInRegistries.ITEM.getKey(stack.item) ?: return null

            val config = ItemConfig()
            config.name = stack.displayName.string
            config.material = id.toString()
            config.unbreakable = stack.get(DataComponents.UNBREAKABLE) != null

            config.lore = stack.get(DataComponents.LORE)?.lines()?.map { it.string } ?: emptyList()
            config.enchantments =
                stack.get(DataComponents.ENCHANTMENTS)?.entrySet()?.associate { (enchant, level) ->
                    enchant.registeredName to level.toUInt()
                } ?: emptyMap()

            return ModItem(stack, config)
        }
    }
}
