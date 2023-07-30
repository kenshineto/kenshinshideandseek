package dev.tylerm.khs.util;

import dev.tylerm.khs.world.VoidGenerator;
import dev.tylerm.khs.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static dev.tylerm.khs.configuration.Config.spawnPatch;

public class Location {

    private final String world;
    private final double x;
    private final double y;
    private final double z;

    public static Location getDefault() {
        return new Location(
                "",
                0.0,
                0.0,
                0.0
        );
    }

    public static Location from(Player player) {
        org.bukkit.Location location = player.getLocation();
        return new Location(
                player.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }

    public Location(@NotNull String world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location(@NotNull String world, @NotNull org.bukkit.Location location) {
        this.world = world;
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }

    public World load(WorldType type, World.Environment environment) {
        boolean mapSave = world.startsWith("hs_");
        World bukkitWorld = Bukkit.getWorld(world);
        if(bukkitWorld != null) return bukkitWorld;
        WorldCreator creator = new WorldCreator(world);
        if(type != null) {
            creator.type(type);
        }
        if(environment != null) {
            creator.environment(environment);
        }
        if(mapSave) {
            creator.generator(new VoidGenerator());
        }
        Bukkit.getServer().createWorld(creator).setAutoSave(!mapSave);
        return Bukkit.getWorld(world);
    }

    public World load() {
        if(!exists()) return null;
        if(!Main.getInstance().isLoaded()) return null;
        return load(null, null);
    }

    private org.bukkit.Location toBukkit() {
        return new org.bukkit.Location(
                Bukkit.getWorld(world),
                x,
                y,
                z
        );
    }

    public void teleport(Player player) {
        if(!exists()) return;
        if(load() == null) return;
        if (spawnPatch) {
            Main.getInstance().scheduleTask(() -> player.teleport(toBukkit()));
        } else {
            player.teleport(toBukkit());
        }
    }

    public Location changeWorld(String world) {
        return new Location(
                world,
                x,
                y,
                z
        );
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getBlockX() {
        return (int)x;
    }

    public int getBlockY() {
        return (int)y;
    }

    public int getBlockZ() {
        return (int)z;
    }

    public boolean exists() {
        if(world.equals("")) return false;
        String path = Main.getInstance().getWorldContainer()+File.separator+world;
        File destination = new File(path);
        return destination.isDirectory();
    }

    public boolean isNotSetup() {
        return getBlockX() == 0 && getBlockY() == 0 && getBlockZ() == 0;
    }

    public boolean isNotInBounds(int xmin, int xmax, int zmin, int zmax) {
        return getBlockX() <= xmin || getBlockX() >= xmax || getBlockZ() <= zmin || getBlockZ() >= zmax;
    }

}
