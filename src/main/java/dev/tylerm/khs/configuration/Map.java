package dev.tylerm.khs.configuration;

import java.util.ArrayList;
import java.util.List;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.game.events.Border;
import dev.tylerm.khs.util.Location;
import dev.tylerm.khs.world.WorldLoader;
import org.bukkit.*;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import static dev.tylerm.khs.configuration.Config.*;

public class Map {

    private final String name;

    private dev.tylerm.khs.util.Location
        spawnPosition = dev.tylerm.khs.util.Location.getDefault(),
        lobbyPosition = dev.tylerm.khs.util.Location.getDefault(),
        seekerLobbyPosition = dev.tylerm.khs.util.Location.getDefault();

    private int
        xBoundMin = 0,
        zBoundMin = 0,
        xBoundMax = 0,
        zBoundMax = 0,
        xWorldBorder = 0,
        zWorldBorder = 0,
        worldBorderSize = 0,
        worldBorderDelay = 0,
        worldBorderChange = 0;

    private boolean
        blockhunt = false;

    private List<Material>
        blockhuntBlocks = new ArrayList<>();

    private final Border
        worldBorder;

    private final WorldLoader
        worldLoader;

    public Map(String name) {
        this.name = name;
        this.worldBorder = new Border(this);
        this.worldLoader = new WorldLoader(this);
    }

    public void setSpawn(dev.tylerm.khs.util.Location pos) {
        this.spawnPosition = pos;
    }

    public void setLobby(dev.tylerm.khs.util.Location pos) {
        this.lobbyPosition = pos;
    }

    public void setSeekerLobby(dev.tylerm.khs.util.Location pos) {
        this.seekerLobbyPosition = pos;
    }

    public void setWorldBorderData(int x, int z, int size, int delay, int move) {
        if(size < 1) {
            this.worldBorderSize = 0;
            this.worldBorderDelay = 0;
            this.worldBorderChange = 0;
            this.xWorldBorder = 0;
            this.zWorldBorder = 0;
        } else {
            this.worldBorderSize = size;
            this.worldBorderDelay = delay;
            this.worldBorderChange = move;
            this.xWorldBorder = x;
            this.zWorldBorder = z;
        }
        this.worldBorder.resetWorldBorder();
    }

    public void setBlockhunt(boolean enabled, List<Material> blocks) {
        if (Main.getInstance().supports(9)) {
            this.blockhunt = enabled;
        } else {
            this.blockhunt = false;
        }
        this.blockhuntBlocks = blocks;
    }

    public void setBoundMin(int x, int z) {
        this.xBoundMin = x;
        this.zBoundMin = z;
    }

    public void setBoundMax(int x, int z) {
        this.xBoundMax = x;
        this.zBoundMax = z;
    }

    @NotNull
    public dev.tylerm.khs.util.Location getGameSpawn() {
        if(mapSaveEnabled) {
            return spawnPosition.changeWorld("hs_"+name);
        } else {
            return spawnPosition;
        }
    }

    @NotNull
    public String getGameSpawnName() {
        if(mapSaveEnabled)
            return getGameSpawn().getWorld();
        else
            return getSpawn().getWorld();
    }

    @NotNull
    public dev.tylerm.khs.util.Location getSpawn() {
        return spawnPosition;
    }

    @NotNull
    public String getSpawnName() {
        return getSpawn().getWorld();
    }

    @NotNull
    public dev.tylerm.khs.util.Location getLobby() {
        return lobbyPosition;
    }

    @NotNull
    public String getLobbyName() {
       return getLobby().getWorld();
    }

    @NotNull
    public dev.tylerm.khs.util.Location getSeekerLobby() {
        return seekerLobbyPosition;
    }

    @NotNull
    public String getSeekerLobbyName() {
       return getSeekerLobby().getWorld();
    }

    @NotNull
    public Location getGameSeekerLobby() {
        if(mapSaveEnabled) {
          return seekerLobbyPosition.changeWorld("hs_"+name);
       } else {
         return seekerLobbyPosition;
       }
    }

    public boolean isWorldBorderEnabled() {
      return worldBorderSize > 0;
    }

    @NotNull
    public Vector getWorldBorderPos() {
       return new Vector(
          xWorldBorder,
          0,
           zWorldBorder
       );
    }

    @NotNull
    public Vector getWorldBorderData() {
       return new Vector(
          worldBorderSize,
          worldBorderDelay,
          worldBorderChange
        );
    }

    @NotNull
    public Border getWorldBorder() {
        return worldBorder;
    }

    public boolean isBlockHuntEnabled() {
        return blockhunt;
    }

    @NotNull
    public List<Material> getBlockHunt() {
        return blockhuntBlocks;
    }

    @NotNull
    public Vector getBoundsMin() {
        return new Vector(
          xBoundMin,
          0,
          zBoundMin
        );
    }

    @NotNull
    public Vector getBoundsMax() {
        return new Vector(
            xBoundMax,
            0,
            zBoundMax
        );
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public WorldLoader getWorldLoader() {
        return worldLoader;
    }

    public boolean isNotSetup() {
        if (spawnPosition.getBlockX() == 0 && spawnPosition.getBlockY() == 0 && spawnPosition.getBlockZ() == 0 || !spawnPosition.exists()) return true;
        if (lobbyPosition.getBlockX() == 0 && lobbyPosition.getBlockY() == 0 && lobbyPosition.getBlockZ() == 0 || !lobbyPosition.exists()) return true;
        if (exitPosition == null || exitPosition.getBlockX() == 0 && exitPosition.getBlockY() == 0 && exitPosition.getBlockZ() == 0 || !exitPosition.exists()) return true;
        if (seekerLobbyPosition.getBlockX() == 0 && seekerLobbyPosition.getBlockY() == 0 && seekerLobbyPosition.getBlockZ() == 0 || !seekerLobbyPosition.exists()) return true;
        if (mapSaveEnabled && !getGameSpawn().exists()) return true;
        if (blockhunt && blockhuntBlocks.isEmpty()) return true;
        if(isWorldBorderEnabled() &&
            new Vector(spawnPosition.getX(), 0, spawnPosition.getZ()).distance(new Vector(xWorldBorder, 0, zWorldBorder)) > 100) return true;
        return isBoundsNotSetup();
    }

    public boolean isBoundsNotSetup() {
        if (xBoundMin == 0 || zBoundMin == 0 || xBoundMax == 0 || zBoundMax == 0) return true;
        int xDiff = xBoundMax - xBoundMin;
        int zDiff = zBoundMax - zBoundMin;
        return xDiff < 5 || zDiff < 5;
    }

}
