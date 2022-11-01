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

import net.tylermurphy.hideAndSeek.command.util.Command;
import net.tylermurphy.hideAndSeek.command.location.util.LocationUtils;
import net.tylermurphy.hideAndSeek.command.location.util.Locations;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class SetLobbyLocation extends Command {

	public void execute(Player sender, String[] args) {
		LocationUtils.setLocation(sender, Locations.LOBBY, args[0], map -> {
			map.setLobby(sender.getLocation());
		});
	}

	public String getLabel() {
		return "lobby";
	}
	
	public String getUsage() {
		return "<map>";
	}

	public String getDescription() {
		return "Sets hide and seeks lobby location to current position";
	}

	public List<String> autoComplete(String parameter) {
		if(parameter != null && parameter.equals("map")) {
			return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
		}
		return null;
	}

}
