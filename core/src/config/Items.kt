package cat.freya.khs.config

import cat.freya.khs.KhsTypes

data class KhsItemsConfig(
    @Section("Hider Items") @Comment("Items that hiders are given") var hiderItems: List<ItemConfig>,
    var hiderHelmet: ItemConfig? = null,
    var hiderChestplate: ItemConfig? = null,
    var hiderLeggings: ItemConfig? = null,
    var hiderBoots: ItemConfig? = null,
    @Section("Seeker Items") @Comment("Items that seekers are given") var seekerItems: List<ItemConfig>,
    // Armor provided to seekers
    var seekerHelmet: ItemConfig?,
    var seekerChestplate: ItemConfig?,
    var seekerLeggings: ItemConfig?,
    var seekerBoots: ItemConfig?,
    @Section("Hider Effects")
    @Comment("Effects hiders are given at the start of the round")
    var hiderEffects: List<EffectConfig>,
    @Section("Seeker Effects")
    @Comment("Effects seekers given at the start of the round and when they respawn")
    var seekerEffects: List<EffectConfig>,
) {
    fun migrate(types: KhsTypes) {
        hiderItems.forEach { it.migrate(types) }
        seekerItems.forEach { it.migrate(types) }
    }

    companion object {
        fun default(types: KhsTypes) =
            KhsItemsConfig(
                hiderItems =
                    listOf(
                        ItemConfig(
                            name = "Hider Sword",
                            material = types.stoneSword,
                            lore = listOf("This is the hider sword"),
                            enchantments = mapOf(types.sharpness to 2u),
                            unbreakable = true,
                            slot = 0u,
                        ),
                        ItemConfig(material = types.splashPotion, effect = types.regen, slot = 1u),
                        ItemConfig(material = types.potion, effect = types.instantHeal, slot = 2u),
                    ),
                seekerItems =
                    listOf(
                        ItemConfig(
                            name = "Seeker Sword",
                            material = types.diamondSword,
                            lore = listOf("this is the seeker sword"),
                            enchantments = mapOf(types.sharpness to 1u),
                            unbreakable = true,
                            slot = 0u,
                        ),
                        ItemConfig(
                            name = "Wacky Stick",
                            material = types.stick,
                            lore = listOf("It will launch people very far", "Use wisely!"),
                            enchantments = mapOf(types.knockback to 3u),
                            slot = 1u,
                        ),
                    ),
                seekerHelmet = ItemConfig(material = types.leatherHelmet),
                seekerChestplate = ItemConfig(material = types.leatherChestplate),
                seekerLeggings = ItemConfig(material = types.leatherLeggings),
                seekerBoots =
                    ItemConfig(
                        material = types.leatherBoots,
                        lore = emptyList(),
                        enchantments = mapOf(types.featherFalling to 4u),
                    ),
                hiderEffects =
                    listOf(
                        EffectConfig(
                            type = types.waterBreathing,
                            duration = 1000000u,
                            amplifier = 1u,
                            ambient = false,
                            particles = false,
                        ),
                        EffectConfig(
                            type = types.dolphinsGrace,
                            duration = 1000000u,
                            amplifier = 1u,
                            ambient = false,
                            particles = false,
                        ),
                    ),
                seekerEffects =
                    listOf(
                        EffectConfig(
                            type = types.speedBoost,
                            duration = 1000000u,
                            amplifier = 2u,
                            ambient = false,
                            particles = false,
                        ),
                        EffectConfig(
                            type = types.jumpBoost,
                            duration = 1000000u,
                            amplifier = 1u,
                            ambient = false,
                            particles = false,
                        ),
                        EffectConfig(
                            type = types.waterBreathing,
                            duration = 1000000u,
                            amplifier = 10u,
                            ambient = false,
                            particles = false,
                        ),
                        EffectConfig(
                            type = types.dolphinsGrace,
                            duration = 1000000u,
                            amplifier = 1u,
                            ambient = false,
                            particles = false,
                        ),
                    ),
            )
    }
}
