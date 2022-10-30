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

package net.tylermurphy.hideAndSeek.command;

import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.configuration.Map;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import net.tylermurphy.hideAndSeek.game.util.Status;
import org.bukkit.entity.Player;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class SetBounds implements ICommand {

	public void execute(Player sender, String[] args) {
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
		if (!sender.getWorld().getName().equals(map.getSpawn().getWorld().getName())) {
			sender.sendMessage(errorPrefix + message("BOUNDS_WRONG_WORLD"));
			return;
		}
		if (sender.getLocation().getBlockX() == 0 || sender.getLocation().getBlockZ() == 0) {
			sender.sendMessage(errorPrefix + message("NOT_AT_ZERO"));
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
		} else if (map.getBoundsMax().getX() < sender.getLocation().getBlockZ()) {
			first = false;
			bzs = bzl;
			bzl = sender.getLocation().getBlockZ();
		} else {
			first = false;
			bzs = sender.getLocation().getBlockZ();
		}
		map.setBoundMin(bxs, bzs);
		map.setBoundMax(bxl, bzl);
		sender.sendMessage(messagePrefix + message("BOUNDS").addAmount(first ? 1 : 2));
		saveConfig();
	}

	public String getLabel() {
		return "setBounds";
	}
	
	public String getUsage() {
		return "<map>";
	}

	public String getDescription() {
		return "Sets the map bounds for the game.";
	}

}
