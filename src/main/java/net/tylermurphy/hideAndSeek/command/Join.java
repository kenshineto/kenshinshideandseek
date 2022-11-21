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
import net.tylermurphy.hideAndSeek.command.util.ICommand;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.errorPrefix;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Join implements ICommand {

	public void execute(Player sender, String[] args) {
		if (Main.getInstance().getGame().checkCurrentMap()) {
			sender.sendMessage(errorPrefix + message("GAME_SETUP"));
			return;
		}
		Player player = Bukkit.getServer().getPlayer(sender.getName());
		if (player == null) {
			sender.sendMessage(errorPrefix + message("COMMAND_ERROR"));
			return;
		}
		if (Main.getInstance().getBoard().contains(player)) {
			sender.sendMessage(errorPrefix + message("GAME_INGAME"));
			return;
		}
		if(args.length > 0) {
			if(Main.getInstance().getBoard().size() > 0) {
				sender.sendMessage(errorPrefix + message("LOBBY_IN_USE"));
				return;
			}
			net.tylermurphy.hideAndSeek.configuration.Map map = Maps.getMap(args[0]);
			if(map == null) {
				sender.sendMessage(errorPrefix + message("INVALID_MAP"));
				return;
			}
			Main.getInstance().getGame().setCurrentMap(map);
		}
		Main.getInstance().getGame().join(player);
	}

	public String getLabel() {
		return "join";
	}

	public String getUsage() {
		return "<*map>";
	}

	public String getDescription() {
		return "Joins the lobby if game is set to manual join/leave";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		if(parameter.equals("*map")) {
			return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
		}
		return null;
	}

}
