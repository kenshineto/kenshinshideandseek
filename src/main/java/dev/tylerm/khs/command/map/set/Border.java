package dev.tylerm.khs.command.map.set;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.command.util.ICommand;
import dev.tylerm.khs.configuration.Config;
import dev.tylerm.khs.configuration.Localization;
import dev.tylerm.khs.configuration.Map;
import dev.tylerm.khs.configuration.Maps;
import dev.tylerm.khs.game.util.Status;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Border implements ICommand {

	public void execute(Player sender, String[] args) {
		if (Main.getInstance().getGame().getStatus() != Status.STANDBY) {
			sender.sendMessage(Config.errorPrefix + Localization.message("GAME_INPROGRESS"));
			return;
		}
		Map map = Maps.getMap(args[0]);
		if(map == null) {
			sender.sendMessage(Config.errorPrefix + Localization.message("INVALID_MAP"));
			return;
		}
		if (map.getSpawn().isNotSetup()) {
			sender.sendMessage(Config.errorPrefix + Localization.message("ERROR_GAME_SPAWN"));
			return;
		}

		int num,delay,change;
		try { num = Integer.parseInt(args[1]); } catch (Exception e) {
			sender.sendMessage(Config.errorPrefix + Localization.message("WORLDBORDER_INVALID_INPUT").addAmount(args[0]));
			return;
		}
		try { delay = Integer.parseInt(args[2]); } catch (Exception e) {
			sender.sendMessage(Config.errorPrefix + Localization.message("WORLDBORDER_INVALID_INPUT").addAmount(args[1]));
			return;
		}
		try { change = Integer.parseInt(args[3]); } catch (Exception e) {
			sender.sendMessage(Config.errorPrefix + Localization.message("WORLDBORDER_INVALID_INPUT").addAmount(args[2]));
			return;
		}
		if (num < 100) {
			sender.sendMessage(Config.errorPrefix + Localization.message("WORLDBORDER_MIN_SIZE"));
			return;
		}
		if (change < 1) {
			sender.sendMessage(Config.errorPrefix + Localization.message("WORLDBORDER_CHANGE_SIZE"));
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
		sender.sendMessage(Config.messagePrefix + Localization.message("WORLDBORDER_ENABLE").addAmount(num).addAmount(delay).addAmount(change));
		map.getWorldBorder().resetWorldBorder();
	}

	public String getLabel() {
		return "border";
	}
	
	public String getUsage() {
		return "<map> <size> <delay> <move>";
	}

	public String getDescription() {
		return "Sets a maps world border information";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		if(parameter.equals("map")) {
			return Maps.getAllMaps().stream().map(Map::getName).collect(Collectors.toList());
		}
		return Collections.singletonList(parameter);
	}

}
