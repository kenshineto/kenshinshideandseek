package cat.freya.khs.config

data class KhsItemsConfig(
    @Section("Hider Items")
    @Comment("Items that hiders are given")
    var hiderItems: List<ItemConfig> =
        listOf(
            // Stone sword
            ItemConfig(
                "Hider Sword", // Name
                "STONE_SWORD", // Material
                listOf("This is the hider sword"), // Lore
                mapOf("sharpness" to 2u), // Enchantments
                true,
            ), // Unbreakable
            // Regen potion
            ItemConfig(
                null, // Name
                "SPLASH_POTION:REGEN",
            ), // Material
            // Heal potion
            ItemConfig(
                null, // Name
                "POTION:INSTANT_HEAL",
            ),
        ), // Material
    var hiderHelmet: ItemConfig? = null,
    var hiderChestplate: ItemConfig? = null,
    var hiderLeggings: ItemConfig? = null,
    var hiderBoots: ItemConfig? = null,
    @Section("Seeker Items")
    @Comment("Items that seekers are given")
    var seekerItems: List<ItemConfig> =
        listOf(
            // Diamond sword
            ItemConfig(
                "Seeker Sword", // Name
                "DIAMOND_SWORD", // Material
                listOf("this is the seeker sword"), // Lore
                mapOf("sharpness" to 1u), // Enchantments
                true,
            ), // Unbreakable
            // Wacky stick
            ItemConfig(
                "Wacky Stick", // Name
                "STICK", // Material
                listOf("It will launch people very far", "Use wisely!"), // Lore
                mapOf("knockback" to 3u),
            ),
        ), // Enchantments

    // Armor provided to seekers
    var seekerHelmet: ItemConfig? = ItemConfig(null, "LEATHER_HELMET"),
    var seekerChestplate: ItemConfig? = ItemConfig(null, "LEATHER_CHESTPLATE"),
    var seekerLeggings: ItemConfig? = ItemConfig(null, "LEATHER_LEGGINGS"),
    var seekerBoots: ItemConfig? =
        ItemConfig(
            null, // Name
            "LEATHER_BOOTS", // Material
            emptyList(), // Lore
            mapOf("feather_falling" to 4u),
        ), // Enchantments
    @Section("Hider Effects")
    @Comment("Effects hiders are given at the start of the round")
    var hiderEffects: List<EffectConfig> =
        listOf(
            EffectConfig(
                "WATER_BREATHING", // Type
                1000000u, // Duration
                1u, // Amplifier
                false, // Ambient
                false,
            ), // Particles
            EffectConfig(
                "DOLPHINS_GRACE", // Type
                1000000u, // Duration
                1u, // Amplifier
                false, // Ambient
                false,
            ),
        ), // Particles
    @Section("Seeker Effects")
    @Comment("Effects seekers given at the start of the round and when they respawn")
    var seekerEffects: List<EffectConfig> =
        listOf(
            EffectConfig(
                "SPEED", // Type
                1000000u, // Duration
                2u, // Amplifier
                false, // Ambient
                false,
            ), // Particles
            EffectConfig(
                "JUMP", // Type
                1000000u, // Duration
                1u, // Amplifier
                false, // Ambient
                false,
            ), // Particles
            EffectConfig(
                "WATER_BREATHING", // Type
                1000000u, // Duration
                10u, // Amplifier
                false, // Ambient
                false,
            ), // Particles
            EffectConfig(
                "DOLPHINS_GRACE", // Type
                1000000u, // Duration
                1u, // Amplifier
                false, // Ambient
                false,
            ),
        ), // Particles
)
