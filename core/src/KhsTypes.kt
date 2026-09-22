package cat.freya.khs

data class KhsTypes(
    // Blocks
    val air: String,
    val furnace: String,
    val craftingTable: String,
    val anvil: String,
    val chest: String,
    val barrel: String,
    val barrier: String,
    val redWool: String,
    val greenWool: String,
    // Items
    val bed: String,
    val clock: String,
    val compass: String,
    val feather: String,
    val snowball: String,
    val stick: String,
    val stoneSword: String,
    val diamondSword: String,
    val potion: String,
    val splashPotion: String,
    val lingeringPotion: String,
    val enchantedBook: String,
    val playerHead: String,
    val skeletonSkull: String,
    // Armor
    val leatherHelmet: String,
    val leatherChestplate: String,
    val leatherLeggings: String,
    val leatherBoots: String,
    val goldenChestplate: String,
    val ironChestplate: String,
    // Enchantments
    val featherFalling: String,
    val knockback: String,
    val sharpness: String,
    // Sounds
    val noteBlockBaseDrum: String,
    val noteBlockPling: String,
    val playerHurt: String,
    val anvilLand: String,
    // Effects
    val dolphinsGrace: String,
    val instantHeal: String,
    val jumpBoost: String,
    val regen: String,
    val speedBoost: String,
    val waterBreathing: String,
) {
    val potionTypes = listOf(potion, splashPotion, lingeringPotion)

    fun isPotion(type: String): Boolean = potionTypes.contains(type)
}
