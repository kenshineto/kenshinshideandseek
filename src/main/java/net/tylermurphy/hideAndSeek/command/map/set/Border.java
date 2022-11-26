package net.tylermurphy.hideAndSeek.command.map.set;

import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.command.util.ICommand;
import net.tylermurphy.hideAndSeek.configuration.Map;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import net.tylermurphy.hideAndSeek.game.util.Status;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Border implements ICommand {

	public void execute(Player sender, String[] args) {
		if (Main.getInstance().getGame().getStatus() != Status.STANDBY) {
			sender.sendMessage(errorPrefix + message("GAME_INPROGRESS"));
			return;
		}
		Map map = Maps.getMap(args[0]);
		if(map == null) {
			sender.sendMessage(errorPrefix + message("INVALID_MAP"));
			return;
		}
		if (map.getSpawn().isNotSetup()) {
			sender.sendMessage(errorPrefix + message("ERROR_GAME_SPAWN"));
			return;
		}
		if (args.length < 4) {
			map.setWorldBorderData(0, 0, 0, 0, 0);
			addToConfig("worldBorder.enabled",false);
			saveConfig();
			sender.sendMessage(messagePrefix + message("WORLDBORDER_DISABLE"));
			Main.getInstance().getGame().getCurrentMap().getWorldBorder().resetWorldBorder();
			return;
		}
		int num,delay,change;
		try { num = Integer.parseInt(args[1]); } catch (Exception e) {
			sender.sendMessage(errorPrefix + message("WORLDBORDER_INVALID_INPUT").addAmount(args[0]));
			return;
		}
		try { delay = Integer.parseInt(args[2]); } catch (Exception e) {
			sender.sendMessage(errorPrefix + message("WORLDBORDER_INVALID_INPUT").addAmount(args[1]));
			return;
		}
		try { change = Integer.parseInt(args[3]); } catch (Exception e) {
			sender.sendMessage(errorPrefix + message("WORLDBORDER_INVALID_INPUT").addAmount(args[2]));
			return;
		}
		if (num < 100) {
			sender.sendMessage(errorPrefix + message("WORLDBORDER_MIN_SIZE"));
			return;
		}
		if (change < 1) {
			sender.sendMessage(errorPrefix + message("WORLDBORDER_CHANGE_SIZE"));
			return;
		}
		map.setWorldBorderData(
				sender.getLocation().getBlockX(),
				sender.getLocation().getBlockZ(),
				num,
				delay,
				change
		);
		Maps.setMap(map.getName(), map);
		sender.sendMessage(messagePrefix + message("WORLDBORDER_ENABLE").addAmount(num).addAmount(delay).addAmount(change));
		map.getWorldBorder().resetWorldBorder();
	}

	public String getLabel() {
		return "border";
	}
	
	public String getUsage() {
		return "<map> <*size> <*delay> <*move>";
	}

	public String getDescription() {
		return "Sets a maps world border information";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		if(parameter.equals("map")) {
			return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
		}
		return Collections.singletonList(parameter);
	}

}
