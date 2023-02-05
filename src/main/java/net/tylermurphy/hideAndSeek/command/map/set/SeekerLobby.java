package net.tylermurphy.hideAndSeek.command.map.set;

import net.tylermurphy.hideAndSeek.command.util.ICommand;
import net.tylermurphy.hideAndSeek.command.location.LocationUtils;
import net.tylermurphy.hideAndSeek.command.location.Locations;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import net.tylermurphy.hideAndSeek.util.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.warningPrefix;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class SeekerLobby implements ICommand {

    public void execute(Player sender, String[] args) {
        LocationUtils.setLocation(sender, Locations.SEEKER, args[0], map -> {
            if(map.getSpawn().isNotSetup()) {
                throw new RuntimeException(message("GAME_SPAWN_NEEDED").toString());
            }
            if(!map.getSpawnName().equals(sender.getLocation().getWorld().getName())) {
                throw new RuntimeException(message("SEEKER_LOBBY_INVALID").toString());
            }
            map.setSeekerLobby(Location.from(sender));
            if(!map.isBoundsNotSetup()) {
                Vector boundsMin = map.getBoundsMin();
                Vector boundsMax = map.getBoundsMax();
                if(map.getSeekerLobby().isNotInBounds(boundsMin.getBlockX(), boundsMax.getBlockX(), boundsMin.getBlockZ(), boundsMax.getBlockZ())) {
                    sender.sendMessage(warningPrefix + message("WARN_MAP_BOUNDS"));
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
            return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
        }
        return null;
    }

}
