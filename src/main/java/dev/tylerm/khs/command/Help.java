package dev.tylerm.khs.command;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.command.util.ICommand;
import dev.tylerm.khs.util.Pair;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static dev.tylerm.khs.configuration.Config.errorPrefix;
import static dev.tylerm.khs.configuration.Localization.message;

public class Help implements ICommand {

	public void execute(Player sender, String[] args) {
		final int pageSize = 4;
		List<Pair<String, ICommand>> commands = Main.getInstance().getCommandGroup().getCommands();
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
		for(Pair<String, ICommand> pair : commands.stream().skip((long) (page - 1) * pageSize).limit(pageSize).collect(Collectors.toList())) {
			ICommand command = pair.getRight();
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
