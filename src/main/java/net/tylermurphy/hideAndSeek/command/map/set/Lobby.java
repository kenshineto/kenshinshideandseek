package net.tylermurphy.hideAndSeek.command.map.set;

import net.tylermurphy.hideAndSeek.command.util.ICommand;
import net.tylermurphy.hideAndSeek.command.location.LocationUtils;
import net.tylermurphy.hideAndSeek.command.location.Locations;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import net.tylermurphy.hideAndSeek.util.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class Lobby implements ICommand {

	public void execute(Player sender, String[] args) {
		LocationUtils.setLocation(sender, Locations.LOBBY, args[0], map -> {
			map.setLobby(Location.from(sender));
		});
	}

	public String getLabel() {
		return "lobby";
	}
	
	public String getUsage() {
		return "<map>";
	}

	public String getDescription() {
		return "Sets the maps lobby location";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		if(parameter.equals("map")) {
			return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
		}
		return null;
	}

}
