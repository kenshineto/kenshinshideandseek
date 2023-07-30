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

import java.util.List;
import java.util.stream.Collectors;

public class Spawn implements ICommand {

	public void execute(Player sender, String[] args) {
		LocationUtils.setLocation(sender, Locations.GAME, args[0], map -> {

			if (map.isWorldBorderEnabled() &&
					new Vector(sender.getLocation().getX(), 0, sender.getLocation().getZ()).distance(map.getWorldBorderPos()) > 100) {
				sender.sendMessage(Config.errorPrefix + Localization.message("WORLDBORDER_POSITION"));
				throw new RuntimeException("World border not enabled or not in valid position!");
			}

			map.setSpawn(Location.from(sender));

			if(!map.isBoundsNotSetup()) {
				Vector boundsMin = map.getBoundsMin();
				Vector boundsMax = map.getBoundsMax();
				if(map.getSpawn().isNotInBounds(boundsMin.getBlockX(), boundsMax.getBlockX(), boundsMin.getBlockZ(), boundsMax.getBlockZ())) {
					sender.sendMessage(Config.warningPrefix + Localization.message("WARN_MAP_BOUNDS"));
				}
			}

			if(map.getSeekerLobby().getWorld() != null && !map.getSeekerLobby().getWorld().equals(sender.getLocation().getWorld().getName())) {
				sender.sendMessage(Config.warningPrefix + Localization.message("SEEKER_LOBBY_SPAWN_RESET"));
				map.setSeekerLobby(Location.getDefault());
			}

			if (!sender.getLocation().getWorld().getName().equals(map.getSpawnName()) && Config.mapSaveEnabled) {
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
			return Maps.getAllMaps().stream().map(Map::getName).collect(Collectors.toList());
		}
		return null;
	}

}
