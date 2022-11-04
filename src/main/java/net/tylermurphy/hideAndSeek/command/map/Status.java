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

package net.tylermurphy.hideAndSeek.command.map;

import net.tylermurphy.hideAndSeek.command.util.Command;
import net.tylermurphy.hideAndSeek.configuration.Map;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Status extends Command {
	
	public void execute(Player sender, String[] args) {
		
		String msg = message("SETUP").toString();
		int count = 0;
		Map map = Maps.getMap(args[0]);
		if(map == null) {
			sender.sendMessage(errorPrefix + message("INVALID_MAP"));
			return;
		}
		if (map.getSpawn().getBlockX() == 0 && map.getSpawn().getBlockY() == 0 && map.getSpawn().getBlockZ() == 0 || Map.worldDoesntExist(map.getLobbyName())) {
			msg = msg + "\n" + message("SETUP_GAME");
			count++;
		}
		if (map.getLobby().getBlockX() == 0 && map.getLobby().getBlockY() == 0 && map.getLobby().getBlockZ() == 0 || Map.worldDoesntExist(map.getLobbyName())) {
			msg = msg + "\n" + message("SETUP_LOBBY");
			count++;
		}
		if (map.getSeekerLobby().getBlockX() == 0 && map.getSeekerLobby().getBlockY() == 0 && map.getSeekerLobby().getBlockZ() == 0 || Map.worldDoesntExist(map.getSeekerLobbyName())) {
			msg = msg + "\n" + message("SETUP_SEEKER_LOBBY");
			count++;
		}
		if (exitPosition.getBlockX() == 0 && exitPosition.getBlockY() == 0 && exitPosition.getBlockZ() == 0 || Map.worldDoesntExist(exitWorld)) {
			msg = msg + "\n" + message("SETUP_EXIT");
			count++;
		}
		if (map.isBoundsNotSetup()) {
			msg = msg + "\n" + message("SETUP_BOUNDS");
			count++;
		}
		if (mapSaveEnabled && Map.worldDoesntExist(map.getGameSpawnName())) {
			msg = msg + "\n" + message("SETUP_SAVEMAP");
			count++;
		}
		if (map.isBlockHuntEnabled() && map.getBlockHunt().isEmpty()) {
			msg = msg + "\n" + message("SETUP_BLOCKHUNT");
		}
		if (count < 1) {
			sender.sendMessage(messagePrefix + message("SETUP_COMPLETE"));
		} else {
			sender.sendMessage(msg);
		}
	}

	public String getLabel() {
		return "status";
	}

	public String getUsage() {
		return "<map>";
	}

	public String getDescription() {
		return "Shows what needs to be setup on a map";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		if(parameter.equals("map")) {
			return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
		}
		return null;
	}

}