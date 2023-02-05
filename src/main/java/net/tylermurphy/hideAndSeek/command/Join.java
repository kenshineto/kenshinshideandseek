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
