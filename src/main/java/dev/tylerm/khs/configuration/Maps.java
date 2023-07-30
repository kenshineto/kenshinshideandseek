package dev.tylerm.khs.configuration;

import java.util.*;
import java.util.stream.Collectors;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.util.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.cryptomorin.xseries.XMaterial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Maps {

    private static final HashMap<String, Map> MAPS = new HashMap<>();

    @Nullable
    public static Map getMap(String name) {
        return MAPS.get(name);
    }

    @Nullable
    public static Map getRandomMap() {
        Optional<Map> map;
        if(MAPS.values().size() > 0) {
            Collection<Map> setupMaps = MAPS.values().stream().filter(m -> !m.isNotSetup()).collect(Collectors.toList());
            if(setupMaps.size() < 1) {
                return null;
            }
            map = setupMaps.stream().skip(new Random().nextInt(setupMaps.size())).findFirst();
        } else {
            map = Optional.empty();
        }
        return map.orElse(null);
    }

    public static void setMap(String name, Map map) {
        MAPS.put(name, map);
        saveMaps();
    }

    public static boolean removeMap(String name) {
        boolean status = MAPS.remove(name) != null;
        saveMaps();
        return status;
    }

    @NotNull
    public static Collection<Map> getAllMaps() {
        return MAPS.values();
    }

    public static void loadMaps() {

        ConfigManager manager = ConfigManager.create("maps.yml");

        ConfigurationSection maps = manager.getConfigurationSection("maps");
        if(maps == null) return;
        Set<String> keys = maps.getKeys(false);
        if(keys == null) return;

        MAPS.clear();
        for(String key : keys) {
            MAPS.put(key, parseMap(maps, key));
        }

    }

    private static Map parseMap(ConfigurationSection maps, String name) {
        ConfigurationSection data = maps.getConfigurationSection(name);
        if(data == null) return null;
        Map map = new Map(name);
        Main.getInstance().getLogger().info("Loading map: " + name + "...");
        map.setSpawn(getSpawn(data, "game"));
        map.setLobby(getSpawn(data, "lobby"));
        map.setSeekerLobby(getSpawn(data, "seeker"));
        map.setBoundMin(data.getInt("bounds.min.x"), data.getInt("bounds.min.z"));
        map.setBoundMax(data.getInt("bounds.max.x"), data.getInt("bounds.max.z"));
        map.setWorldBorderData(
                data.getInt("worldborder.pos.x"),
                data.getInt("worldborder.pos.z"),
                data.getInt("worldborder.size"),
                data.getInt("worldborder.delay"),
                data.getInt("worldborder.change")
        );
        List<String> blockhunt = data.getStringList("blockhunt.blocks");
        if(blockhunt == null) blockhunt = new ArrayList<>();
        map.setBlockhunt(
            data.getBoolean("blockhunt.enabled"),
            blockhunt
            .stream()
            .map(XMaterial::matchXMaterial)
            .filter(Optional::isPresent)
            .map(e -> e.get().parseMaterial())
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
        );
        return map;
    }

    private static Location getSpawn(ConfigurationSection data, String spawn) {
        String world = data.getString("spawns."+spawn+".world");
        double x = data.getDouble("spawns."+spawn+".x");
        double y = data.getDouble("spawns."+spawn+".y");
        double z = data.getDouble("spawns."+spawn+".z");
        return new Location(world, x, y, z);
    }

    private static void saveMaps() {

        ConfigManager manager = ConfigManager.create("maps.yml");
        ConfigurationSection maps = new YamlConfiguration();

        for(Map map : MAPS.values()) {
            ConfigurationSection data = new YamlConfiguration();
            saveSpawn(data, map.getSpawn(), "game", map);
            saveSpawn(data, map.getLobby(), "lobby", map);
            saveSpawn(data, map.getSeekerLobby(), "seeker", map);
            data.set("bounds.min.x", map.getBoundsMin().getX());
            data.set("bounds.min.z", map.getBoundsMin().getZ());
            data.set("bounds.max.x", map.getBoundsMax().getX());
            data.set("bounds.max.z", map.getBoundsMax().getZ());
            data.set("worldborder.pos.x", map.getWorldBorderPos().getX());
            data.set("worldborder.pos.z", map.getWorldBorderPos().getZ());
            data.set("worldborder.pos.size", map.getWorldBorderData().getX());
            data.set("worldborder.pos.delay", map.getWorldBorderData().getY());
            data.set("worldborder.pos.change", map.getWorldBorderData().getZ());
            data.set("blockhunt.enabled", map.isBlockHuntEnabled());
            data.set("blockhunt.blocks", map.getBlockHunt().stream().map(Material::name).collect(Collectors.toList()));
            maps.set(map.getName(), data);
        }

        manager.set("maps", maps);
        manager.overwriteConfig();

    }

    private static void saveSpawn(ConfigurationSection data, Location spawn, String name, Map map) {
        String worldName = getWorldName(name, map);
        data.set("spawns." + name + ".world", worldName);
        data.set("spawns." + name + ".x", spawn.getX());
        data.set("spawns." + name + ".y", spawn.getY());
        data.set("spawns." + name + ".z", spawn.getZ());
    }

    private static String getWorldName(String name, Map map) {
        switch (name) {
            case "game": return map.getSpawnName();
            case "lobby": return map.getLobbyName();
            case "seeker": return map.getSeekerLobbyName();
            default: return null;
        }
    }

}
