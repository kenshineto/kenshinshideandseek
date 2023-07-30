package dev.tylerm.khs.command.map;

import dev.tylerm.khs.command.util.ICommand;
import dev.tylerm.khs.configuration.Config;
import dev.tylerm.khs.configuration.Localization;
import dev.tylerm.khs.configuration.Map;
import dev.tylerm.khs.configuration.Maps;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class Status implements ICommand {
	
	public void execute(Player sender, String[] args) {
		
		String msg = Localization.message("SETUP").toString();
		int count = 0;
		Map map = Maps.getMap(args[0]);
		if(map == null) {
			sender.sendMessage(Config.errorPrefix + Localization.message("INVALID_MAP"));
			return;
		}
		if (map.getSpawn().getBlockX() == 0 && map.getSpawn().getBlockY() == 0 && map.getSpawn().getBlockZ() == 0 || !map.getSpawn().exists()) {
			msg = msg + "\n" + Localization.message("SETUP_GAME");
			count++;
		}
		if (map.getLobby().getBlockX() == 0 && map.getLobby().getBlockY() == 0 && map.getLobby().getBlockZ() == 0 || !map.getLobby().exists()) {
			msg = msg + "\n" + Localization.message("SETUP_LOBBY");
			count++;
		}
		if (map.getSeekerLobby().getBlockX() == 0 && map.getSeekerLobby().getBlockY() == 0 && map.getSeekerLobby().getBlockZ() == 0 || !map.getSeekerLobby().exists()) {
			msg = msg + "\n" + Localization.message("SETUP_SEEKER_LOBBY");
			count++;
		}
		if (Config.exitPosition.getBlockX() == 0 && Config.exitPosition.getBlockY() == 0 && Config.exitPosition.getBlockZ() == 0 || !Config.exitPosition.exists()) {
			msg = msg + "\n" + Localization.message("SETUP_EXIT");
			count++;
		}
		if (map.isBoundsNotSetup()) {
			msg = msg + "\n" + Localization.message("SETUP_BOUNDS");
			count++;
		}
		if (Config.mapSaveEnabled && !map.getGameSpawn().exists()) {
			msg = msg + "\n" + Localization.message("SETUP_SAVEMAP");
			count++;
		}
		if (map.isBlockHuntEnabled() && map.getBlockHunt().isEmpty()) {
			msg = msg + "\n" + Localization.message("SETUP_BLOCKHUNT");
            count++;
		}
		if (count < 1) {
			sender.sendMessage(Config.messagePrefix + Localization.message("SETUP_COMPLETE"));
		} else {
			sender.sendMessage(msg);
		}
	}

	public String getLabel() {
		return "status";
	}

	public String getUsage() {
		return "<map>";
	}

	public String getDescription() {
		return "Shows what needs to be setup on a map";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		if(parameter.equals("map")) {
			return Maps.getAllMaps().stream().map(Map::getName).collect(Collectors.toList());
		}
		return null;
	}

}
