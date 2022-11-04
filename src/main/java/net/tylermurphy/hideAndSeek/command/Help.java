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
import net.tylermurphy.hideAndSeek.command.util.Command;
import net.tylermurphy.hideAndSeek.util.Pair;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.errorPrefix;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Help extends Command {

	public void execute(Player sender, String[] args) {
		final int pageSize = 4;
		List<Pair<String, Command>> commands = Main.getInstance().getCommandGroup().getCommands();
		int pages = (commands.size() - 1) / pageSize + 1;
		int page;
		try {
			if(args.length < 1) {
				page = 1;
			} else {
				page = Integer.parseInt(args[0]);
				if (page < 1) {
					throw new IllegalArgumentException("Inavlid Input");
				}
			}
		} catch (Exception e) {
			sender.sendMessage(errorPrefix + message("WORLDBORDER_INVALID_INPUT").addAmount(args[0]));
			return;
		}
		String spacer = ChatColor.GRAY + "?" + ChatColor.WHITE;
		StringBuilder message = new StringBuilder();
		message.append(String.format("%s================ %sHelp: Page (%s/%s) %s================",
				ChatColor.AQUA, ChatColor.WHITE, page, pages, ChatColor.AQUA));
		int lines = 0;
		for(Pair<String, Command> pair : commands.stream().skip((long) (page - 1) * pageSize).limit(pageSize).collect(Collectors.toList())) {
			Command command = pair.getRight();
			String label = pair.getLeft();
			String start = label.substring(0, label.indexOf(" "));
			String invoke = label.substring(label.indexOf(" ")+1);
			message.append(String.format("\n%s %s/%s %s%s %s%s\n%s  %s%s%s",
					spacer,
					ChatColor.AQUA,
					start,
					ChatColor.WHITE,
					invoke,
					ChatColor.BLUE,
					command.getUsage(),
					spacer,
					ChatColor.GRAY,
					ChatColor.ITALIC,
					command.getDescription()
			));
			lines += 2;
		}
		if(lines / 2 < pageSize) {
			for(int i = 0; i < pageSize * 2 - lines; i++) {
				message.append("\n").append(spacer);
			}
		}
		message.append("\n").append(ChatColor.AQUA).append("===============================================");
		sender.sendMessage(message.toString());
	}

	public String getLabel() {
		return "help";
	}

	public String getUsage() {
		return "<*page>";
	}

	public String getDescription() {
		return "Get the commands for the plugin";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		return Collections.singletonList(parameter);
	}

}
