package net.tylermurphy.hideAndSeek.command.map.blockhunt;

import net.tylermurphy.hideAndSeek.command.location.LocationUtils;
import net.tylermurphy.hideAndSeek.command.location.Locations;
import net.tylermurphy.hideAndSeek.command.util.Command;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Enabled extends Command {

    public void execute(Player sender, String[] args) {
        LocationUtils.setLocation(sender, Locations.LOBBY, args[0], map -> {
            map.setLobby(sender.getLocation());
        });
    }

    public String getLabel() {
        return "enabled";
    }

    public String getUsage() {
        return "<map> <bool>";
    }

    public String getDescription() {
        return "Sets hide and seeks lobby location to current position";
    }

    public List<String> autoComplete(String parameter) {
        if(parameter != null && parameter.equals("map")) {
            return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
        }
        if(parameter != null && parameter.equals("bool")) {
            return Arrays.asList("true", "false");
        }
        return null;
    }

}
