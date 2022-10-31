package net.tylermurphy.hideAndSeek.command.location;

import net.tylermurphy.hideAndSeek.command.ICommand;
import net.tylermurphy.hideAndSeek.command.location.util.LocationUtils;
import net.tylermurphy.hideAndSeek.command.location.util.Locations;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class SetSeekerLobbyLocation  implements ICommand {

    public void execute(Player sender, String[] args) {
        LocationUtils.setLocation(sender, Locations.SEEKER, args[0], map -> {
            if(map.isSpawnNotSetup()) {
                throw new RuntimeException(message("GAME_SPAWN_NEEDED").toString());
            }
            if(!map.getSpawnName().equals(sender.getLocation().getWorld().getName())) {
                throw new RuntimeException(message("SEEKER_LOBBY_INVALID").toString());
            }
            map.setSeekerLobby(sender.getLocation());
        });
    }

    public String getLabel() {
        return "setseekerlobby";
    }

    public String getUsage() {
        return "<map>";
    }

    public String getDescription() {
        return "Sets hide and seeks seeker lobby location to current position";
    }

    public List<String> autoComplete(String parameter) {
        if(parameter != null && parameter.equals("map")) {
            return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
        }
        return null;
    }

}
