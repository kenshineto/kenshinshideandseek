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

import net.tylermurphy.hideAndSeek.command.map.Save;
import net.tylermurphy.hideAndSeek.util.Pair;
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
		if (args.length < 1 || !commandRegister.containsKey(args[0].toLowerCase()) ) {
			if (permissionsRequired && !player.hasPermission("hs.about")) {
				player.sendMessage(errorPrefix + message("COMMAND_NOT_ALLOWED"));
			} else {
				player.sendMessage(
						String.format("%s%sKenshin's Hide and Seek %s(%s1.7.0 BETA%s)\n", ChatColor.AQUA, ChatColor.BOLD, ChatColor.GRAY,ChatColor.WHITE,ChatColor.GRAY) +
						String.format("%sAuthor: %s[KenshinEto]\n", ChatColor.GRAY, ChatColor.WHITE) +
						String.format("%sHelp Command: %s/hs %shelp", ChatColor.GRAY, ChatColor.AQUA, ChatColor.WHITE)
				);
			}
		} else {
			String invoke = args[0].toLowerCase();
			if (!invoke.equals("about") && !invoke.equals("help") && Save.runningBackup) {
				player.sendMessage(errorPrefix + message("MAPSAVE_INPROGRESS"));
			} else {
				try {
					Object object = commandRegister.get(invoke);

					if(object instanceof CommandGroup) {
						CommandGroup group = (CommandGroup) object;
						return group.handleCommand(player, permission+"."+group.getLabel(), Arrays.copyOfRange(args, 1, args.length));
					} else if(object instanceof Command) {
						Command command = (Command) object;

						if (permissionsRequired && !player.hasPermission(permission+"."+command.getLabel())) {
							player.sendMessage(errorPrefix + message("COMMAND_NOT_ALLOWED"));
							return true;
						}

						int parameterCount = (int) Arrays.stream(command.getUsage().split(" ")).filter(p -> p.startsWith("<") && !p.startsWith("<*")).count();
						if(args.length - 1 < parameterCount) {
							player.sendMessage(errorPrefix + message("ARGUMENT_COUNT"));
							return true;
						}

						command.execute(player,Arrays.copyOfRange(args, 1, args.length));
					}
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
							if (object instanceof Command) {
								Command command = (Command) object;
								return player.hasPermission(command.getLabel());
							} else if (object instanceof CommandGroup) {
								CommandGroup group = (CommandGroup) object;
								return group.hasPermission(player, group.getLabel());
							}
							return false;
						})
						.collect(Collectors.toList());
			} else {
				return commandRegister.keySet()
						.stream()
						.filter(handle -> handle.toLowerCase().startsWith(invoke))
						.collect(Collectors.toList());
			}
		} else {
			if (commandRegister.containsKey(invoke)) {
				Object object = commandRegister.get(invoke);
				if (object instanceof CommandGroup) {
					CommandGroup group = (CommandGroup) object;
					return group.handleTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
				} else if (object instanceof Command) {
					Command command = (Command) object;
					String[] usage = command.getUsage().split(" ");
					if (args.length - 2 < usage.length) {
						String parameter = usage[args.length - 2];
						String name = parameter.replace("<", "").replace(">", "");
						List<String> list = command.autoComplete(name, args[args.length - 1]);
						if (list != null) {
							return list;
						}
					}
				}
			}
			return new ArrayList<>();
		}
	}

	private boolean hasPermission(Player player, String permission) {
		for(Object object : commandRegister.values()) {
			if(object instanceof Command) {
				Command command = (Command) object;
				if(player.hasPermission(permission+"."+command.getLabel())) return true;
			} else if(object instanceof CommandGroup) {
				CommandGroup group = (CommandGroup) object;
				if (group.hasPermission(player, permission+"."+group.getLabel())) return true;
			}
		}
		return false;
	}

	public List<Pair<String, Command>> getCommands() {
		return getCommands(this.getLabel());
	}

	private List<Pair<String, Command>> getCommands(String prefix) {
		List<Pair<String, Command>> commands = new LinkedList<>();
		for(Object object : commandRegister.values()) {
			if(object instanceof Command) {
				Command command = (Command) object;
				commands.add(new Pair<>(prefix+" "+command.getLabel(), command));
			} else if(object instanceof CommandGroup) {
				CommandGroup group = (CommandGroup) object;
				commands.addAll(group.getCommands(prefix+" "+group.getLabel()));
			}
		}
		return commands;
	}


}
