package net.tylermurphy.hideAndSeek.command.location;

import net.tylermurphy.hideAndSeek.command.ICommand;
import net.tylermurphy.hideAndSeek.command.location.util.LocationUtils;
import net.tylermurphy.hideAndSeek.command.location.util.Locations;
import org.bukkit.entity.Player;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;

public class SetSeekerLobbyLocation  implements ICommand {

    public void execute(Player sender, String[] args) {
        LocationUtils.setLocation(sender, Locations.SEEKER, args[0], map -> {
            map.setSeekerLobby(sender.getLocation());
        });
    }

    public String getLabel() {
        return "setseekerlobby";
    }

    public String getUsage() {
        return "";
    }

    public String getDescription() {
        return "Sets hide and seeks seeker lobby location to current position";
    }

}
