package cat.freya.khs.fabric

import cat.freya.khs.world.Effect as KhsEffect
import cat.freya.khs.config.EffectConfig
import cat.freya.khs.world.Item as KhsItem
import cat.freya.khs.config.ItemConfig
import net.minecraft.world.effect.MobEffect
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.ItemStack
import java.util.UUID

class FabricKhsItem(val inner: ItemStack, override val config: ItemConfig): KhsItem {
    override val name: String? = (inner.displayName ?: inner.itemName)?.string
    override fun clone(): KhsItem = FabricKhsItem(inner, config)
}

class FabricKhsEffect(val inner: Holder<MobEffect> override val config: EffectConfig): KhsEffect {
    override val name: String? = inner.value().displayName.toString()
    override fun clone(): KhsEffect = FabricKhsEffect(inner, config)
}
