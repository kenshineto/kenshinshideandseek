package cat.freya.khs.bukkit

import cat.freya.khs.KhsTypes

fun bukkitTypes(plugin: KhsPlugin) =
    KhsTypes(
        // Blocks
        air = "AIR",
        furnace = "FURNACE",
        craftingTable = "CRAFTING_TABLE",
        anvil = "ANVIL",
        chest = "CHEST",
        barrel = "BARREL",
        barrier = "BARRIER",
        redWool = "RED_WOOL",
        greenWool = "GREEN_WOOL",
        // Items
        bed = "BED",
        clock = "CLOCK",
        compass = "COMPASS",
        feather = "FEATHER",
        snowball = "SNOWBALL",
        stick = "STICK",
        stoneSword = "STONE_SWORD",
        diamondSword = "DIAMOND_SWORD",
        potion = "POTION",
        splashPotion = "SPLASH_POTION",
        lingeringPotion = "LINGERING_POTION",
        enchantedBook = "ENCHANTED_BOOK",
        playerHead = "PLAYER_HEAD",
        skeletonSkull = "SKELETON_SKULL",
        // Armor
        leatherHelmet = "LEATHER_HELMET",
        leatherChestplate = "LEATHER_CHESTPLATE",
        leatherLeggings = "LEATHER_LEGGINGS",
        leatherBoots = "LEATHER_BOOTS",
        goldenChestplate = "GOLDEN_CHESTPLATE",
        ironChestplate = "IRON_CHESTPLATE",
        // Enchantments
        featherFalling = "feather_falling",
        knockback = "knockback",
        sharpness = "sharpness",
        // Sounds
        noteBlockBaseDrum = "BLOCK_NOTE_BLOCK_BASEDRUM",
        noteBlockPling = "BLOCK_NOTE_BLOCK_PLING",
        playerHurt = if (plugin.shim.supports(9)) "ENTITY_PLAYER_DEATH" else "ENTITY_PLAYER_HURT",
        anvilLand = "ANVIL_LAND",
        // Effects
        dolphinsGrace = "DOLPHINS_GRACE",
        instantHeal = "INSTANT_HEAL",
        jumpBoost = "JUMP",
        regen = "REGEN",
        speedBoost = "SPEED",
        waterBreathing = "WATER_BREATHING",
    )
