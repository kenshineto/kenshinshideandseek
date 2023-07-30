package dev.tylerm.khs.configuration;

import com.cryptomorin.xseries.XItemStack;
import dev.tylerm.khs.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Items {

    public static List<ItemStack> HIDER_ITEMS, SEEKER_ITEMS;
    public static ItemStack 
        HIDER_HELM, SEEKER_HELM,
        HIDER_CHEST, SEEKER_CHEST,
        HIDER_LEGS, SEEKER_LEGS,
        HIDER_BOOTS, SEEKER_BOOTS;

    public static List<PotionEffect> HIDER_EFFECTS, SEEKER_EFFECTS;

    public static void loadItems() {

        ConfigManager manager = ConfigManager.create("items.yml");

        SEEKER_ITEMS = new ArrayList<>();
        SEEKER_HELM = null;
        SEEKER_CHEST = null;
        SEEKER_LEGS = null;
        SEEKER_BOOTS = null;

        ConfigurationSection SeekerItems = manager.getConfigurationSection("items.seeker");

        for (int i = 0; i < 9; i++) {
            ConfigurationSection section = SeekerItems.getConfigurationSection(String.valueOf(i));
            if (section == null) {
                SEEKER_ITEMS.add(null);
                continue;
            }
            ItemStack item = createItem(section);
            SEEKER_ITEMS.add(item);
        }

        ConfigurationSection SeekerHelmet = SeekerItems.getConfigurationSection("helmet");
        if (SeekerHelmet != null) {
            ItemStack item = createItem(SeekerHelmet);
            if (item != null) {
                SEEKER_HELM = item;
            }
        }

        ConfigurationSection SeekerChestplate = SeekerItems.getConfigurationSection("chestplate");
        if (SeekerChestplate != null) {
            ItemStack item = createItem(SeekerChestplate);
            if (item != null) {
                SEEKER_CHEST = item;
            }
        }

        ConfigurationSection SeekerLeggings = SeekerItems.getConfigurationSection("leggings");
        if (SeekerLeggings != null) {
            ItemStack item = createItem(SeekerLeggings);
            if (item != null) {
                SEEKER_LEGS = item;
            }
        }

        ConfigurationSection SeekerBoots = SeekerItems.getConfigurationSection("boots");
        if (SeekerBoots != null) {
            ItemStack item = createItem(SeekerBoots);
            if (item != null) {
                SEEKER_BOOTS = item;
            }
        }

        HIDER_ITEMS = new ArrayList<>();
        HIDER_HELM = null;
        HIDER_CHEST = null;
        HIDER_LEGS = null;
        HIDER_BOOTS = null;

        ConfigurationSection HiderItems = manager.getConfigurationSection("items.hider");

        for (int i = 0; i < 9; i++) {
            ConfigurationSection section = HiderItems.getConfigurationSection(String.valueOf(i));
            if (section == null) {
                HIDER_ITEMS.add(null);
                continue;
            }
            ItemStack item = createItem(section);
            HIDER_ITEMS.add(item);
        }

        ConfigurationSection HiderHelmet = HiderItems.getConfigurationSection("helmet");
        if (HiderHelmet != null) {
            ItemStack item = createItem(HiderHelmet);
            if (item != null) {
                HIDER_HELM = item;
            }
        }

        ConfigurationSection HiderChestplate = HiderItems.getConfigurationSection("chestplate");
        if (HiderChestplate != null) {
            ItemStack item = createItem(HiderChestplate);
            if (item != null) {
                HIDER_CHEST = item;
            }
        }

        ConfigurationSection HiderLeggings = HiderItems.getConfigurationSection("leggings");
        if (HiderLeggings != null) {
            ItemStack item = createItem(HiderLeggings);
            if (item != null) {
                HIDER_LEGS = item;
            }
        }

        ConfigurationSection HiderBoots = HiderItems.getConfigurationSection("boots");
        if (HiderBoots != null) {
            ItemStack item = createItem(HiderBoots);
            if (item != null) {
                HIDER_BOOTS = item;
            }
        }

        SEEKER_EFFECTS = new ArrayList<>();
        ConfigurationSection SeekerEffects = manager.getConfigurationSection("effects.seeker");

        int i = 1;
        while (true) {
            ConfigurationSection section = SeekerEffects.getConfigurationSection(String.valueOf(i));
            if (section == null) break;
            PotionEffect effect = getPotionEffect(section);
            if (effect != null) SEEKER_EFFECTS.add(effect);
            i++;
        }

        HIDER_EFFECTS = new ArrayList<>();
        ConfigurationSection HiderEffects = manager.getConfigurationSection("effects.hider");
        i = 1;
        while (true) {
            ConfigurationSection section = HiderEffects.getConfigurationSection(String.valueOf(i));
            if (section == null) break;
            PotionEffect effect = getPotionEffect(section);
            if (effect != null) HIDER_EFFECTS.add(effect);
            i++;
        }
    }

    private static ItemStack createItem(ConfigurationSection item) {
        ConfigurationSection config = new YamlConfiguration().createSection("temp");
        String material = item.getString("material").toUpperCase();
        boolean splash = false;
        if (!Main.getInstance().supports(9)) {
            if (material.contains("POTION")) {
                config.set("level", 1);
            }
            if (material.equalsIgnoreCase("SPLASH_POTION") || material.equalsIgnoreCase("LINGERING_POTION")) {
                material = "POTION";
                splash = true;
            }
        }
        config.set("name", item.getString("name"));
        config.set("material", material);
        config.set("enchants", item.getConfigurationSection("enchantments"));
        config.set("unbreakable", item.getBoolean("unbreakable"));
        if (Main.getInstance().supports(14)) {
            if (item.contains("model-data")) {
                config.set("model-data", item.getInt("model-data"));
            }
        }
        if (item.isSet("lore"))
            config.set("lore", item.getStringList("lore"));
        if (material.equalsIgnoreCase("POTION") || material.equalsIgnoreCase("SPLASH_POTION") || material.equalsIgnoreCase("LINGERING_POTION"))
            config.set("base-effect", String.format("%s,%s,%s", item.getString("type"), false, splash));
        ItemStack stack = XItemStack.deserialize(config);
        int amt = item.getInt("amount");
        if (amt < 1) amt = 1;
        stack.setAmount(amt);
        if (stack.getData().getItemType() == Material.AIR) return null;
        return stack;
    }

    private static PotionEffect getPotionEffect(ConfigurationSection item) {
        String type = item.getString("type");
        if (type == null) return null;
        if (PotionEffectType.getByName(type.toUpperCase()) == null) return null;
        return new PotionEffect(
                PotionEffectType.getByName(type.toUpperCase()),
                item.getInt("duration"),
                item.getInt("amplifier"),
                item.getBoolean("ambient"),
                item.getBoolean("particles")
        );
    }

    public static boolean matchItem(ItemStack stack){
        for(ItemStack check : HIDER_ITEMS)
            if(equals(stack,check)) return true;
        for(ItemStack check : SEEKER_ITEMS)
            if(equals(stack,check)) return true;
        return false;
    }

    private static boolean equals(ItemStack a, ItemStack b) {
        if (a == null || b == null) {
            return false;
        } else if (a == b) {
            return true;
        } else {
            return a.getType() == b.getType() && a.hasItemMeta() == b.hasItemMeta() && (!a.hasItemMeta() || Bukkit.getItemFactory().equals(a.getItemMeta(), b.getItemMeta()));
        }
    }

}
