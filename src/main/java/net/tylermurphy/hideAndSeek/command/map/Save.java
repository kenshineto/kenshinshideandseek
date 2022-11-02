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

import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.command.util.Command;
import net.tylermurphy.hideAndSeek.configuration.Map;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import net.tylermurphy.hideAndSeek.game.util.Status;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Save extends Command {

	public static boolean runningBackup = false;
	
	public void execute(Player sender, String[] args) {
		if (!mapSaveEnabled) {
			sender.sendMessage(errorPrefix + message("MAPSAVE_DISABLED"));
			return;
		}
		if (Main.getInstance().getGame().getStatus() != Status.STANDBY) {
			sender.sendMessage(errorPrefix + message("GAME_INPROGRESS"));
			return;
		}
		Map map = Maps.getMap(args[0]);
		if(map == null) {
			sender.sendMessage(errorPrefix + message("INVALID_MAP"));
			return;
		}
		if (map.isSpawnNotSetup()) {
			sender.sendMessage(errorPrefix + message("ERROR_GAME_SPAWN"));
			return;
		}
		if (map.isBoundsNotSetup()) {
			sender.sendMessage(errorPrefix + message("ERROR_MAP_BOUNDS"));
			return;
		}
		sender.sendMessage(messagePrefix + message("MAPSAVE_START"));
		sender.sendMessage(warningPrefix + message("MAPSAVE_WARNING"));
		World world = map.getSpawn().getWorld();
		if (world == null) {
			sender.sendMessage(warningPrefix + message("MAPSAVE_FAIL_WORLD"));
			return;
		}
		world.save();
		BukkitRunnable runnable = new BukkitRunnable() {
			public void run() {
				sender.sendMessage(
						map.getWorldLoader().save()
						);
				runningBackup = false;
			}
		};
		runnable.runTaskAsynchronously(Main.getInstance());
		runningBackup = true;
	}

	public String getLabel() {
		return "save";
	}

	public String getUsage() {
		return "<map>";
	}

	public String getDescription() {
		return "Saves current map for the game. May lag server.";
	}

	public List<String> autoComplete(String parameter) {
		if(parameter != null && parameter.equals("map")) {
			return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
		}
		return null;
	}

}
