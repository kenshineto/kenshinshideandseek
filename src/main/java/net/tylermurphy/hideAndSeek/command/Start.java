package net.tylermurphy.hideAndSeek.command;

import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.command.util.ICommand;
import net.tylermurphy.hideAndSeek.game.util.Status;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.errorPrefix;
import static net.tylermurphy.hideAndSeek.configuration.Config.minPlayers;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Start implements ICommand {

	public void execute(Player sender, String[] args) {
		if (Main.getInstance().getGame().checkCurrentMap()) {
			sender.sendMessage(errorPrefix + message("GAME_SETUP"));
			return;
		}
		if (Main.getInstance().getGame().getStatus() != Status.STANDBY) {
			sender.sendMessage(errorPrefix + message("GAME_INPROGRESS"));
			return;
		}
		if (!Main.getInstance().getBoard().contains(sender)) {
			sender.sendMessage(errorPrefix + message("GAME_NOT_INGAME"));
			return;
		}
		if (Main.getInstance().getBoard().size() < minPlayers) {
			sender.sendMessage(errorPrefix + message("START_MIN_PLAYERS").addAmount(minPlayers));
			return;
		}
		String seekerName;
		if (args.length < 1) {
			Main.getInstance().getGame().start();
			return;
		} else {
			seekerName = args[0];
		}
		Player seeker = Bukkit.getPlayer(seekerName);
		if (seeker == null || !Main.getInstance().getBoard().contains(seeker)) {
			sender.sendMessage(errorPrefix + message("START_INVALID_NAME").addPlayer(seekerName));
			return;
		}
		Main.getInstance().getGame().start(seeker);
	}
	
	public String getLabel() {
		return "start";
	}
	
	public String getUsage() {
		return "<*player>";
	}

	public String getDescription() {
		return "Starts the game either with a random seeker or chosen one";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		if(parameter.equals("player")) {
			return Main.getInstance().getBoard().getPlayers().stream().map(Player::getName).collect(Collectors.toList());
		}
		return null;
	}

}
