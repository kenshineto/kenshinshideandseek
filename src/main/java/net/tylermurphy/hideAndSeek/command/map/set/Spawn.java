package net.tylermurphy.hideAndSeek.command.map.set;

import net.tylermurphy.hideAndSeek.command.util.ICommand;
import net.tylermurphy.hideAndSeek.command.location.LocationUtils;
import net.tylermurphy.hideAndSeek.command.location.Locations;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import net.tylermurphy.hideAndSeek.util.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Spawn implements ICommand {

	public void execute(Player sender, String[] args) {
		LocationUtils.setLocation(sender, Locations.GAME, args[0], map -> {

			if (map.isWorldBorderEnabled() &&
					new Vector(sender.getLocation().getX(), 0, sender.getLocation().getZ()).distance(map.getWorldBorderPos()) > 100) {
				sender.sendMessage(errorPrefix + message("WORLDBORDER_POSITION"));
				throw new RuntimeException("World border not enabled or not in valid position!");
			}

			map.setSpawn(Location.from(sender));

			if(!map.isBoundsNotSetup()) {
				Vector boundsMin = map.getBoundsMin();
				Vector boundsMax = map.getBoundsMax();
				if(map.getSpawn().isNotInBounds(boundsMin.getBlockX(), boundsMax.getBlockX(), boundsMin.getBlockZ(), boundsMax.getBlockZ())) {
					sender.sendMessage(warningPrefix + message("WARN_MAP_BOUNDS"));
				}
			}

			if(map.getSeekerLobby().getWorld() != null && !map.getSeekerLobby().getWorld().equals(sender.getLocation().getWorld().getName())) {
				sender.sendMessage(warningPrefix + message("SEEKER_LOBBY_SPAWN_RESET"));
				map.setSeekerLobby(Location.getDefault());
			}

			if (!sender.getLocation().getWorld().getName().equals(map.getSpawnName()) && mapSaveEnabled) {
				map.getWorldLoader().unloadMap();
			}
		});
	}

	public String getLabel() {
		return "spawn";
	}
	
	public String getUsage() {
		return "<map>";
	}

	public String getDescription() {
		return "Sets the maps game spawn location";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		if(parameter.equals("map")) {
			return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
		}
		return null;
	}

}
