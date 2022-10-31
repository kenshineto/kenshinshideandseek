/*
 * This file is part of Kenshins Hide and Seek
 *
 * Copyright (c) 2021 Tyler Murphy.
 *
 * Kenshins Hide and Seek free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * he Free Software Foundation version 3.
 *
 * Kenshins Hide and Seek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package net.tylermurphy.hideAndSeek.command.location;

import net.tylermurphy.hideAndSeek.command.ICommand;
import net.tylermurphy.hideAndSeek.command.location.util.LocationUtils;
import net.tylermurphy.hideAndSeek.command.location.util.Locations;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class SetSpawnLocation implements ICommand {

	public void execute(Player sender, String[] args) {
		LocationUtils.setLocation(sender, Locations.GAME, args[0], map -> {

			if (map.isWorldBorderEnabled() &&
					new Vector(sender.getLocation().getX(), 0, sender.getLocation().getZ()).distance(map.getWorldBorderPos()) > 100) {
				sender.sendMessage(errorPrefix + message("WORLDBORDER_POSITION"));
				throw new RuntimeException("World border not enabled or not in valid position!");
			}

			map.setSpawn(sender.getLocation());

			if(map.getSeekerLobby().getWorld() != null && !map.getSeekerLobby().getWorld().getName().equals(sender.getLocation().getWorld().getName())) {
				sender.sendMessage(message("SEEKER_LOBBY_SPAWN_RESET").toString());
				map.setSeekerLobby(new Location(null, 0, 0, 0));
			}

			if (!sender.getLocation().getWorld().getName().equals(map.getSpawnName()) && mapSaveEnabled) {
				map.getWorldLoader().unloadMap();
			}
		});
	}

	public String getLabel() {
		return "setspawn";
	}
	
	public String getUsage() {
		return "<map>";
	}

	public String getDescription() {
		return "Sets hide and seeks spawn location to current position";
	}

	public List<String> autoComplete(String parameter) {
		if(parameter != null && parameter.equals("map")) {
			return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
		}
		return null;
	}

}
