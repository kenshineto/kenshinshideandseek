package dev.tylerm.khs.command.util;

import dev.tylerm.khs.configuration.Config;
import dev.tylerm.khs.configuration.Localization;
import dev.tylerm.khs.util.Pair;
import dev.tylerm.khs.util.Tuple;
import dev.tylerm.khs.command.map.Save;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CommandGroup {

	private final Map<String, Object> commandRegister;
	private final String label;

	public CommandGroup(String label, Object... data) {
		this.label = label;
		this.commandRegister = new LinkedHashMap<>();
		for(Object o : data) {
			registerCommand(o);
		}
	}

	public String getLabel() {
		return label;
	}

	private void registerCommand(Object object) {
		if (object instanceof ICommand) {
			ICommand command = (ICommand) object;
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
	
	public void handleCommand(Player player, String[] args) {

		Tuple<ICommand, String, String[]> data = getCommand(args, this.getLabel());

		if (data == null) {
			player.sendMessage(
					String.format("%s%sKenshin's Hide and Seek %s(%s1.7.6%s)\n", ChatColor.AQUA, ChatColor.BOLD, ChatColor.GRAY, ChatColor.WHITE, ChatColor.GRAY) +
							String.format("%sAuthor: %s[KenshinEto]\n", ChatColor.GRAY, ChatColor.WHITE) +
							String.format("%sHelp Command: %s/hs %shelp", ChatColor.GRAY, ChatColor.AQUA, ChatColor.WHITE)
			);
			return;
		}

		ICommand command = data.getLeft();
		String permission = data.getCenter();
		String[] parameters = data.getRight();

		if (Save.runningBackup) {
			player.sendMessage(Config.errorPrefix + Localization.message("MAPSAVE_INPROGRESS"));
			return;
		}

		if (Config.permissionsRequired && !player.hasPermission(permission)) {
			player.sendMessage(Config.errorPrefix + Localization.message("COMMAND_NOT_ALLOWED"));
			return;
		}

		int parameterCount = (int) Arrays.stream(command.getUsage().split(" ")).filter(p -> p.startsWith("<") && !p.startsWith("<*")).count();
		if(parameters.length < parameterCount) {
			player.sendMessage(Config.errorPrefix + Localization.message("ARGUMENT_COUNT"));
			return;
		}

		try {
			command.execute(player, parameters);
		}	catch (Exception e) {
			player.sendMessage(Config.errorPrefix + "An error has occurred.");
			e.printStackTrace();
		}
	}

	@Nullable
	private Tuple<ICommand, String, String[]> getCommand(String[] args, String permission) {
		if(args.length < 1) {
			return null;
		}
		String invoke = args[0];
		if(commandRegister.containsKey(invoke)) {
			Object o = commandRegister.get(invoke);
			if (o instanceof CommandGroup) {
				CommandGroup group = (CommandGroup) o;
				return group.getCommand(
						Arrays.copyOfRange(args, 1, args.length),
						permission + "." + group.getLabel()
				);
			} else if(o instanceof ICommand) {
				ICommand command = (ICommand) o;
				return new Tuple<>(command, permission + "." + command.getLabel(), Arrays.copyOfRange(args, 1, args.length));
			}
		}
		return null;
	}

	public List<String> handleTabComplete(Player player, String[] args) {
		return handleTabComplete(player, this.getLabel(), args);
	}

	private List<String> handleTabComplete(Player player, String permission, String[] args) {
		String invoke = args[0].toLowerCase();
		if (args.length == 1) {
			return new ArrayList<>(commandRegister.keySet())
					.stream()
					.filter(handle -> handle.toLowerCase().startsWith(invoke))
					.filter(handle -> {
						Object object = commandRegister.get(handle);
						if (object instanceof ICommand) {
							ICommand command = (ICommand) object;
							return !Config.permissionsRequired || player.hasPermission(permission + "." + command.getLabel());
						} else if (object instanceof CommandGroup) {
							CommandGroup group = (CommandGroup) object;
							return !Config.permissionsRequired || group.hasPermission(player, permission + "." + group.getLabel());
						}
						return false;
					})
					.collect(Collectors.toList());
		} else {
			if (commandRegister.containsKey(invoke)) {
				Object object = commandRegister.get(invoke);
				if (object instanceof CommandGroup) {
					CommandGroup group = (CommandGroup) object;
					return group.handleTabComplete(player, permission + "." + group.getLabel(), Arrays.copyOfRange(args, 1, args.length));
				} else if (object instanceof ICommand) {
					ICommand command = (ICommand) object;
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
			if(object instanceof ICommand) {
				ICommand command = (ICommand) object;
				if(player.hasPermission(permission + command.getLabel())) return true;
			} else if(object instanceof CommandGroup) {
				CommandGroup group = (CommandGroup) object;
				if (group.hasPermission(player, permission + this.label + ".")) return true;
			}
		}
		return false;
	}

	public List<Pair<String, ICommand>> getCommands() {
		return getCommands(this.getLabel());
	}

	private List<Pair<String, ICommand>> getCommands(String prefix) {
		List<Pair<String, ICommand>> commands = new LinkedList<>();
		for(Object object : commandRegister.values()) {
			if(object instanceof ICommand) {
				ICommand command = (ICommand) object;
				commands.add(new Pair<>(prefix+" "+command.getLabel(), command));
			} else if(object instanceof CommandGroup) {
				CommandGroup group = (CommandGroup) object;
				commands.addAll(group.getCommands(prefix+" "+group.getLabel()));
			}
		}
		return commands;
	}


}
