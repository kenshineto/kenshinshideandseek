package cat.freya.khs.config

data class KhsItemsConfig(
    @Section("Hider Items")
    @Comment("Items that hiders are given")
    var hiderItems: List<ItemConfig> =
        listOf(
            ItemConfig(
                name = "Hider Sword",
                material = "STONE_SWORD",
                lore = listOf("This is the hider sword"),
                enchantments = mapOf("sharpness" to 2u),
                unbreakable = true,
            ),
            ItemConfig(material = "SPLASH_POTION:REGEN"),
            ItemConfig(material = "POTION:INSTANT_HEAL"),
        ),
    var hiderHelmet: ItemConfig? = null,
    var hiderChestplate: ItemConfig? = null,
    var hiderLeggings: ItemConfig? = null,
    var hiderBoots: ItemConfig? = null,
    @Section("Seeker Items")
    @Comment("Items that seekers are given")
    var seekerItems: List<ItemConfig> =
        listOf(
            ItemConfig(
                name = "Seeker Sword",
                material = "DIAMOND_SWORD",
                lore = listOf("this is the seeker sword"),
                enchantments = mapOf("sharpness" to 1u),
                unbreakable = true,
            ),
            ItemConfig(
                name = "Wacky Stick",
                material = "STICK",
                lore = listOf("It will launch people very far", "Use wisely!"),
                enchantments = mapOf("knockback" to 3u),
            ),
        ),

    // Armor provided to seekers
    var seekerHelmet: ItemConfig? = ItemConfig(material = "LEATHER_HELMET"),
    var seekerChestplate: ItemConfig? = ItemConfig(material = "LEATHER_CHESTPLATE"),
    var seekerLeggings: ItemConfig? = ItemConfig(material = "LEATHER_LEGGINGS"),
    var seekerBoots: ItemConfig? =
        ItemConfig(
            material = "LEATHER_BOOTS",
            lore = emptyList(),
            enchantments = mapOf("feather_falling" to 4u),
        ),
    @Section("Hider Effects")
    @Comment("Effects hiders are given at the start of the round")
    var hiderEffects: List<EffectConfig> =
        listOf(
            EffectConfig(
                type = "WATER_BREATHING",
                duration = 1000000u,
                amplifier = 1u,
                ambient = false,
                particles = false,
            ),
            EffectConfig(
                type = "DOLPHINS_GRACE",
                duration = 1000000u,
                amplifier = 1u,
                ambient = false,
                particles = false,
            ),
        ),
    @Section("Seeker Effects")
    @Comment("Effects seekers given at the start of the round and when they respawn")
    var seekerEffects: List<EffectConfig> =
        listOf(
            EffectConfig(
                type = "SPEED",
                duration = 1000000u,
                amplifier = 2u,
                ambient = false,
                particles = false,
            ),
            EffectConfig(
                type = "JUMP",
                duration = 1000000u,
                amplifier = 1u,
                ambient = false,
                particles = false,
            ),
            EffectConfig(
                type = "WATER_BREATHING",
                duration = 1000000u,
                amplifier = 10u,
                ambient = false,
                particles = false,
            ),
            EffectConfig(
                type = "DOLPHINS_GRACE",
                duration = 1000000u,
                amplifier = 1u,
                ambient = false,
                particles = false,
            ),
        ),
)
