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

import net.tylermurphy.hideAndSeek.command.util.Command;
import net.tylermurphy.hideAndSeek.command.location.LocationUtils;
import net.tylermurphy.hideAndSeek.command.location.Locations;
import org.bukkit.entity.Player;

import java.util.List;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;

public class SetExitLocation extends Command {

	public void execute(Player sender, String[] args) {
		LocationUtils.setLocation(sender, Locations.EXIT, null, map -> {
			addToConfig("exit.x", sender.getLocation().getBlockX());
			addToConfig("exit.y", sender.getLocation().getBlockY());
			addToConfig("exit.z", sender.getLocation().getBlockZ());
			addToConfig("exit.world", sender.getLocation().getWorld().getName());
			exitPosition = sender.getLocation();
			exitWorld = sender.getLocation().getWorld().getName();
			saveConfig();
		});
	}

	public String getLabel() {
		return "setexit";
	}
	
	public String getUsage() {
		return "";
	}

	public String getDescription() {
		return "Sets hide and seeks exit location to current position and world";
	}

	public List<String> autoComplete(String parameter) {
		return null;
	}

}
