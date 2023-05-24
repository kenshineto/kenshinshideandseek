package net.tylermurphy.hideAndSeek.command.map;

import net.tylermurphy.hideAndSeek.command.util.ICommand;
import net.tylermurphy.hideAndSeek.configuration.Map;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Status implements ICommand {
	
	public void execute(Player sender, String[] args) {
		
		String msg = message("SETUP").toString();
		int count = 0;
		Map map = Maps.getMap(args[0]);
		if(map == null) {
			sender.sendMessage(errorPrefix + message("INVALID_MAP"));
			return;
		}
		if (map.getSpawn().getBlockX() == 0 && map.getSpawn().getBlockY() == 0 && map.getSpawn().getBlockZ() == 0 || !map.getSpawn().exists()) {
			msg = msg + "\n" + message("SETUP_GAME");
			count++;
		}
		if (map.getLobby().getBlockX() == 0 && map.getLobby().getBlockY() == 0 && map.getLobby().getBlockZ() == 0 || !map.getLobby().exists()) {
			msg = msg + "\n" + message("SETUP_LOBBY");
			count++;
		}
		if (map.getSeekerLobby().getBlockX() == 0 && map.getSeekerLobby().getBlockY() == 0 && map.getSeekerLobby().getBlockZ() == 0 || !map.getSeekerLobby().exists()) {
			msg = msg + "\n" + message("SETUP_SEEKER_LOBBY");
			count++;
		}
		if (exitPosition.getBlockX() == 0 && exitPosition.getBlockY() == 0 && exitPosition.getBlockZ() == 0 || !exitPosition.exists()) {
			msg = msg + "\n" + message("SETUP_EXIT");
			count++;
		}
		if (map.isBoundsNotSetup()) {
			msg = msg + "\n" + message("SETUP_BOUNDS");
			count++;
		}
		if (mapSaveEnabled && !map.getGameSpawn().exists()) {
			msg = msg + "\n" + message("SETUP_SAVEMAP");
			count++;
		}
		if (map.isBlockHuntEnabled() && map.getBlockHunt().isEmpty()) {
			msg = msg + "\n" + message("SETUP_BLOCKHUNT");
            count++;
		}
		if (count < 1) {
			sender.sendMessage(messagePrefix + message("SETUP_COMPLETE"));
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
			return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
		}
		return null;
	}

}
