package dev.tylerm.khs.command.map;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.command.util.ICommand;
import dev.tylerm.khs.configuration.Config;
import dev.tylerm.khs.configuration.Localization;
import dev.tylerm.khs.configuration.Map;
import dev.tylerm.khs.configuration.Maps;
import dev.tylerm.khs.game.util.Status;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class Save implements ICommand {

	public static boolean runningBackup = false;
	
	public void execute(Player sender, String[] args) {
		if (!Config.mapSaveEnabled) {
			sender.sendMessage(Config.errorPrefix + Localization.message("MAPSAVE_DISABLED"));
			return;
		}
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
		if (map.isBoundsNotSetup()) {
			sender.sendMessage(Config.errorPrefix + Localization.message("ERROR_MAP_BOUNDS"));
			return;
		}
		sender.sendMessage(Config.messagePrefix + Localization.message("MAPSAVE_START"));
		sender.sendMessage(Config.warningPrefix + Localization.message("MAPSAVE_WARNING"));
		World world = map.getSpawn().load();
		if (world == null) {
			sender.sendMessage(Config.warningPrefix + Localization.message("MAPSAVE_FAIL_WORLD"));
			return;
		}
		world.save();
		BukkitRunnable runnable = new BukkitRunnable() {
			public void run() {
				sender.sendMessage(
						map.getWorldLoader().save()
						);
				runningBackup = false;
			}
		};
		runnable.runTaskAsynchronously(Main.getInstance());
		runningBackup = true;
	}

	public String getLabel() {
		return "save";
	}

	public String getUsage() {
		return "<map>";
	}

	public String getDescription() {
		return "Saves the map to its own separate playable map";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		if(parameter.equals("map")) {
			return Maps.getAllMaps().stream().map(Map::getName).collect(Collectors.toList());
		}
		return null;
	}

}
