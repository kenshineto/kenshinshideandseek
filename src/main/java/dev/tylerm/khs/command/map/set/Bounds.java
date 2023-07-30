package dev.tylerm.khs.command.map.set;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.command.util.ICommand;
import dev.tylerm.khs.configuration.Config;
import dev.tylerm.khs.configuration.Localization;
import dev.tylerm.khs.configuration.Map;
import dev.tylerm.khs.configuration.Maps;
import dev.tylerm.khs.game.util.Status;
import dev.tylerm.khs.util.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class Bounds implements ICommand {

	public void execute(Player sender, String[] args) {
		if (Main.getInstance().getGame().getStatus() != Status.STANDBY) {
			sender.sendMessage(Config.errorPrefix + Localization.message("GAME_INPROGRESS"));
			return;
		}
		Map map = Maps.getMap(args[0]);
		if(map == null) {
			sender.sendMessage(Config.errorPrefix + Localization.message("INVALID_MAP"));
			return;
		}
		if (map.getSpawn().isNotSetup()) {
			sender.sendMessage(Config.errorPrefix + Localization.message("ERROR_GAME_SPAWN"));
			return;
		}
		if (map.getSeekerLobby().isNotSetup()) {
			sender.sendMessage(Config.errorPrefix + Localization.message("ERROR_GAME_SEEKER_SPAWN"));
			return;
		}
		if (!sender.getWorld().getName().equals(map.getSpawnName())) {
			sender.sendMessage(Config.errorPrefix + Localization.message("BOUNDS_WRONG_WORLD"));
			return;
		}
		if (sender.getLocation().getBlockX() == 0 || sender.getLocation().getBlockZ() == 0) {
			sender.sendMessage(Config.errorPrefix + Localization.message("NOT_AT_ZERO"));
			return;
		}
		boolean first = true;
		int bxs = map.getBoundsMin().getBlockX();
		int bzs = map.getBoundsMin().getBlockZ();
		int bxl = map.getBoundsMax().getBlockX();
		int bzl = map.getBoundsMax().getBlockZ();
		if (bxs != 0 && bzs != 0 && bxl != 0 && bzl != 0) {
			bxs = bzs = bxl = bzl = 0;
		}
		if (bxl == 0) {
			bxl = sender.getLocation().getBlockX();
		} else if (map.getBoundsMax().getX() < sender.getLocation().getBlockX()) {
			first = false;
			bxs = bxl;
			bxl = sender.getLocation().getBlockX();
		} else {
			first = false;
			bxs = sender.getLocation().getBlockX();
		}
		if (bzl == 0) {
			bzl = sender.getLocation().getBlockZ();
		} else if (map.getBoundsMax().getZ() < sender.getLocation().getBlockZ()) {
			first = false;
			bzs = bzl;
			bzl = sender.getLocation().getBlockZ();
		} else {
			first = false;
			bzs = sender.getLocation().getBlockZ();
		}
		map.setBoundMin(bxs, bzs);
		map.setBoundMax(bxl, bzl);
		if(!map.isBoundsNotSetup()) {
			if(!map.getSpawn().isNotSetup()) {
				if(map.getSpawn().isNotInBounds(bxs, bxl, bzs, bzl)) {
					map.setSpawn(Location.getDefault());
					sender.sendMessage(Config.warningPrefix + Localization.message("WARN_SPAWN_RESET"));
				}
			}
			if(!map.getSeekerLobby().isNotSetup()) {
				if(map.getSeekerLobby().isNotInBounds(bxs, bxl, bzs, bzl)) {
					map.setSeekerLobby(Location.getDefault());
					sender.sendMessage(Config.warningPrefix + Localization.message("WARN_SEEKER_SPAWN_RESET"));
				}
			}
		}
		Maps.setMap(map.getName(), map);
		sender.sendMessage(Config.messagePrefix + Localization.message("BOUNDS").addAmount(first ? 1 : 2));
	}

	public String getLabel() {
		return "bounds";
	}
	
	public String getUsage() {
		return "<map>";
	}

	public String getDescription() {
		return "Sets the map bounds for the game.";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		if(parameter.equals("map")) {
			return Maps.getAllMaps().stream().map(Map::getName).collect(Collectors.toList());
		}
		return null;
	}

}
