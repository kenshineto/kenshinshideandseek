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
		if (spawnPosition == null) {
			sender.sendMessage(errorPrefix + message("ERROR_GAME_SPAWN"));
			return;
		}
		if (!sender.getWorld().getName().equals(spawnWorld)) {
			sender.sendMessage(errorPrefix + message("BOUNDS_WRONG_WORLD"));
			return;
		}
		if (sender.getLocation().getBlockX() == 0 || sender.getLocation().getBlockZ() == 0) {
			sender.sendMessage(errorPrefix + message("NOT_AT_ZERO"));
			return;
		}
		boolean first = true;
		if (saveMinX != 0 && saveMinZ != 0 && saveMaxX != 0 && saveMaxZ != 0) {
			saveMinX = 0; saveMinZ= 0; saveMaxX = 0; saveMaxZ = 0;
		}
		if (saveMaxX == 0) {
			addToConfig("bounds.max.x", sender.getLocation().getBlockX());
			saveMaxX = sender.getLocation().getBlockX();
		} else if (saveMaxX < sender.getLocation().getBlockX()) {
			first = false;
			addToConfig("bounds.max.x", sender.getLocation().getBlockX());
			addToConfig("bounds.min.x", saveMaxX);
			saveMinX = saveMaxX;
			saveMaxX = sender.getLocation().getBlockX();
		} else {
			first = false;
			addToConfig("bounds.min.x", sender.getLocation().getBlockX());
			saveMinX = sender.getLocation().getBlockX();
		}
		if (saveMaxZ == 0) {
			addToConfig("bounds.max.z", sender.getLocation().getBlockZ());
			saveMaxZ = sender.getLocation().getBlockZ();
		} else if (saveMaxZ < sender.getLocation().getBlockZ()) {
			first = false;
			addToConfig("bounds.max.z", sender.getLocation().getBlockZ());
			addToConfig("bounds.min.z", saveMaxZ);
			saveMinZ = saveMaxZ;
			saveMaxZ = sender.getLocation().getBlockZ();
		} else {
			first = false;
			addToConfig("bounds.min.z", sender.getLocation().getBlockZ());
			saveMinZ = sender.getLocation().getBlockZ();
		}
		sender.sendMessage(messagePrefix + message("BOUNDS").addAmount(first ? 1 : 2));
		saveConfig();
	}

	public String getLabel() {
		return "setBounds";
	}
	
	public String getUsage() {
		return "";
	}

	public String getDescription() {
		return "Sets the map bounds for the game.";
	}

}
