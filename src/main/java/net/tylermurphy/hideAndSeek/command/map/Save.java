package net.tylermurphy.hideAndSeek.command.map;

import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.command.util.ICommand;
import net.tylermurphy.hideAndSeek.configuration.Map;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import net.tylermurphy.hideAndSeek.game.util.Status;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Save implements ICommand {

	public static boolean runningBackup = false;
	
	public void execute(Player sender, String[] args) {
		if (!mapSaveEnabled) {
			sender.sendMessage(errorPrefix + message("MAPSAVE_DISABLED"));
			return;
		}
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
		if (map.isBoundsNotSetup()) {
			sender.sendMessage(errorPrefix + message("ERROR_MAP_BOUNDS"));
			return;
		}
		sender.sendMessage(messagePrefix + message("MAPSAVE_START"));
		sender.sendMessage(warningPrefix + message("MAPSAVE_WARNING"));
		World world = map.getSpawn().load();
		if (world == null) {
			sender.sendMessage(warningPrefix + message("MAPSAVE_FAIL_WORLD"));
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
			return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
		}
		return null;
	}

}
