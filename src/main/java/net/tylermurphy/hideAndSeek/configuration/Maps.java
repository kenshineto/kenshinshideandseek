package net.tylermurphy.hideAndSeek.configuration;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.cryptomorin.xseries.XMaterial;

public class Maps {

    private static final HashMap<String, Map> MAPS = new HashMap<>();

    public static Map getMap(String name) {
        return MAPS.get(name);
    }

    public static Map getRandomMap() {
        Optional<Map> map = MAPS.values().stream().skip(new Random().nextInt(MAPS.values().size())).findFirst();
        if(map.isPresent()) return map.get();
        setMap("default", new Map("default"));
        return MAPS.get("default");
    }

    public static void setMap(String name, Map map) {
        MAPS.put(name, map);
        saveMaps();
    }

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
            System.out.println(key);
            MAPS.put(key, parseMap(maps, key));
        }

    }

    private static Map parseMap(ConfigurationSection maps, String name) {
        ConfigurationSection data = maps.getConfigurationSection(name);
        if(data == null) return null;
        Map map = new Map(name);
        map.setSpawn(setSpawn(data, "game"));
        map.setLobby(setSpawn(data, "lobby"));
        map.setSeekerLobby(setSpawn(data, "seeker"));
        map.setBoundMin(data.getInt("bounds.min.x"), data.getInt("bounds.min.z"));
        map.setBoundMax(data.getInt("bounds.max.x"), data.getInt("bounds.max.z"));
        map.setWorldBorderData(
                data.getInt("worldborder.pos.x"),
                data.getInt("worldborder.pos.z"),
                data.getInt("worldborder.size"),
                data.getInt("worldborder.delay"),
                data.getInt("worldborder.change")
        );
        map.setBlockhunt(
            data.getBoolean("blockhunt.enabled"), 
            data.getStringList("blockhunt.blocks")
            .stream()
            .map(XMaterial::matchXMaterial)
            .filter(Optional::isPresent)
            .map(e -> e.get().parseMaterial())
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
        );
        return map;
    }

    private static Location setSpawn(ConfigurationSection data, String spawn) {
        String worldName = data.getString("spawns."+spawn+".world");
        if(worldName == null) return new Location(null, 0, 0, 0);
        World world = Bukkit.getWorld(worldName);
        if(world == null) return new Location(null, 0, 0, 0);
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
            saveSpawn(data, map.getSpawn(), "game");
            saveSpawn(data, map.getLobby(), "lobby");
            saveSpawn(data, map.getSeekerLobby(), "seeker");
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
            data.set("blockhunt.blocks", map.getBlockHunt().stream().map(Material::name));
            maps.set(map.getName(), map);
        }

        manager.set("maps", maps);
        manager.overwriteConfig();

    }

    private static void saveSpawn(ConfigurationSection data, Location spawn, String name) {
        if(spawn.getWorld() != null) {
            data.set("spawns." + name + ".world", spawn.getWorld().getName());
        } else {
            data.set("spawns." + name + ".world", "world");
        }
        data.set("spawns.." + name + ".x", spawn.getX());
        data.set("spawns.." + name + ".y", spawn.getY());
        data.set("spawns.." + name + ".z", spawn.getZ());
    }

}
