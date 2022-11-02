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

package net.tylermurphy.hideAndSeek.command.util;

import net.tylermurphy.hideAndSeek.command.*;
import net.tylermurphy.hideAndSeek.command.map.Save;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.errorPrefix;
import static net.tylermurphy.hideAndSeek.configuration.Config.permissionsRequired;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class CommandGroup {

	private final Map<String, Object> commandRegister;
	private final String label;

	public CommandGroup(String label, Object... data) {
		this.label = label;
		this.commandRegister = new LinkedHashMap<>();
		for(Object o : data) registerCommand(o);
	}

	public String getLabel() {
		return label;
	}

	private void registerCommand(Object object) {
		if(object instanceof Command) {
			Command command = (Command) object;
			if (!commandRegister.containsKey(command.getLabel())) {
				commandRegister.put(command.getLabel().toLowerCase(), command);
			}
		} else if(object instanceof CommandGroup) {
			CommandGroup group = (CommandGroup) object;
			if (!commandRegister.containsKey(group.getLabel())) {
				commandRegister.put(group.getLabel().toLowerCase(), group);
			}
		}
	}
	
	public boolean handleCommand(Player player, String permission, String[] args) {
		if (args.length < 1 && permission.equals("hs") || !commandRegister.containsKey(args[0].toLowerCase()) ) {
			if (permissionsRequired && !player.hasPermission("hs.about")) {
				player.sendMessage(errorPrefix + message("COMMAND_NOT_ALLOWED"));
			} else {
				player.sendMessage(
						String.format("%s%sHide and Seek %s(%s1.7.0 BETA%s)\n", ChatColor.AQUA, ChatColor.BOLD, ChatColor.GRAY,ChatColor.WHITE,ChatColor.GRAY) +
						String.format("%sAuthor: %s[KenshinEto]\n", ChatColor.GRAY, ChatColor.WHITE) +
						String.format("%sHelp Command: %s/hs %shelp", ChatColor.GRAY, ChatColor.AQUA, ChatColor.WHITE)
				);
			}
		} else {
			String invoke = args[0].toLowerCase();
			if (!invoke.equals("about") && !invoke.equals("help") && Save.runningBackup) {
				player.sendMessage(errorPrefix + message("MAPSAVE_INPROGRESS"));
			} else if (permissionsRequired && !player.hasPermission(permission+"."+invoke)) {
				player.sendMessage(errorPrefix + message("COMMAND_NOT_ALLOWED"));
			} else {
				try {
					Object object = commandRegister.get(invoke);
					if(object instanceof CommandGroup) return ((CommandGroup) object).handleCommand(player, permission+"."+this.label, Arrays.copyOfRange(args, 1, args.length));
					Command command = (Command) object;

					int parameters = (int) Arrays.stream(command.getUsage().split(" ")).filter(p -> p.startsWith("<") && !p.startsWith("<*")).count();
					if(args.length - 1 < parameters) {
						player.sendMessage(errorPrefix + message("ARGUMENT_COUNT"));
						return true;
					}
					command.execute(player,Arrays.copyOfRange(args, 1, args.length));
				} catch (Exception e) {
					player.sendMessage(errorPrefix + "An error has occurred.");
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	public List<String> handleTabComplete(CommandSender sender, String[] args) {
		String invoke = args[0].toLowerCase();
		if (args.length == 1) {
			if(sender instanceof Player) {
				Player player = (Player) sender;
				return new ArrayList<>(commandRegister.keySet())
						.stream()
						.filter(handle -> handle.toLowerCase().startsWith(invoke))
						.filter(handle -> {
							Object object = commandRegister.get(handle);
							if (object instanceof Command) return ((Command) object).hasPermission(player, this.label);
							if (object instanceof CommandGroup)
								return ((CommandGroup) object).hasPermission(player, this.label);
							return false;
						})
						.collect(Collectors.toList());
			}
			return commandRegister.keySet().stream().filter(handle -> handle.toLowerCase().startsWith(invoke)).collect(Collectors.toList());
		} else {
			if (!commandRegister.containsKey(invoke)) {
				return new ArrayList<>();
			} else {
				Object object = commandRegister.get(invoke);
				if(object instanceof CommandGroup) return ((CommandGroup) object).handleTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
				Command command = (Command) object;
				String[] usage = command.getUsage().split(" ");
				List<String> complete;
				if (args.length - 2 < usage.length) {
					String parameter = usage[args.length-2];
					String name = parameter.replace("<", "").replace(">", "");
					complete = command.autoComplete(name);
				} else {
					complete = command.autoComplete(null);
				}
				if(complete == null) return new ArrayList<>();
				else return complete;
			}
		}
	}

	private boolean hasPermission(Player player, String permission) {
		for(Object object : commandRegister.values()) {
			if(object instanceof Command) if(((Command) object).hasPermission(player, this.label)) return true;
			if(object instanceof CommandGroup) if (((CommandGroup) object).hasPermission(player, permission+"."+this.label)) return true;
		}
		return false;
	}

	//	public static void registerCommands() {
//		registerCommand(new About());
//		registerCommand(new Help());
//		registerCommand(new Setup());
//		registerCommand(new Start());
//		registerCommand(new Stop());
//		registerCommand(new SetSpawnLocation());
//		registerCommand(new SetLobbyLocation());
//		registerCommand(new SetSeekerLobbyLocation());
//		registerCommand(new SetExitLocation());
//		registerCommand(new SetBorder());
//		registerCommand(new Reload());
//		registerCommand(new SaveMap());
//		registerCommand(new SetBounds());
//		registerCommand(new Join());
//		registerCommand(new Leave());
//		registerCommand(new Top());
//		registerCommand(new Wins());
//		registerCommand(new Debug());
//		registerCommand(new AddMap());
//		registerCommand(new RemoveMap());
//		registerCommand(new ListMaps());
//		registerCommand(new SetMap());
//	}

}
