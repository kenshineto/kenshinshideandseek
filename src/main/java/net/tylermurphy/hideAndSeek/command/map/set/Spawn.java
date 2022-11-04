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

package net.tylermurphy.hideAndSeek.command.map.set;

import net.tylermurphy.hideAndSeek.command.util.Command;
import net.tylermurphy.hideAndSeek.command.location.LocationUtils;
import net.tylermurphy.hideAndSeek.command.location.Locations;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Spawn extends Command {

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
