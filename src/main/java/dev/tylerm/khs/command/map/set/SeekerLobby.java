package dev.tylerm.khs.command.map.set;

import dev.tylerm.khs.command.location.LocationUtils;
import dev.tylerm.khs.command.location.Locations;
import dev.tylerm.khs.command.util.ICommand;
import dev.tylerm.khs.configuration.*;
import dev.tylerm.khs.util.Location;
import dev.tylerm.khs.configuration.Maps;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;;
import java.util.stream.Collectors;

public class SeekerLobby implements ICommand {

    public void execute(Player sender, String[] args) {
        LocationUtils.setLocation(sender, Locations.SEEKER, args[0], map -> {
            if(map.getSpawn().isNotSetup()) {
                throw new RuntimeException(Localization.message("GAME_SPAWN_NEEDED").toString());
            }
            if(!map.getSpawnName().equals(sender.getLocation().getWorld().getName())) {
                throw new RuntimeException(Localization.message("SEEKER_LOBBY_INVALID").toString());
            }
            map.setSeekerLobby(Location.from(sender));
            if(!map.isBoundsNotSetup()) {
                Vector boundsMin = map.getBoundsMin();
                Vector boundsMax = map.getBoundsMax();
                if(map.getSeekerLobby().isNotInBounds(boundsMin.getBlockX(), boundsMax.getBlockX(), boundsMin.getBlockZ(), boundsMax.getBlockZ())) {
                    sender.sendMessage(Config.warningPrefix + Localization.message("WARN_MAP_BOUNDS"));
                }
            }
        });
    }

    public String getLabel() {
        return "seekerlobby";
    }

    public String getUsage() {
        return "<map>";
    }

    public String getDescription() {
        return "Sets the maps seeker lobby location";
    }

    public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        if(parameter.equals("map")) {
            return Maps.getAllMaps().stream().map(Map::getName).collect(Collectors.toList());
        }
        return null;
    }

}
