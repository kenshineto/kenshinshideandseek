package dev.tylerm.khs.command.map;

import dev.tylerm.khs.command.util.ICommand;
import dev.tylerm.khs.configuration.Config;
import dev.tylerm.khs.configuration.Localization;
import dev.tylerm.khs.configuration.Map;
import dev.tylerm.khs.configuration.Maps;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GoTo implements ICommand {

    public void execute(Player sender, String[] args) {
        Map map = Maps.getMap(args[0]);
        if(map == null) {
            sender.sendMessage(Config.errorPrefix + Localization.message("INVALID_MAP"));
            return;
        }
        if (map.isNotSetup()) {
            sender.sendMessage(Config.errorPrefix + Localization.message("MAP_NOT_SETUP").addAmount(map.getName()));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "spawn":
                map.getSpawn().teleport(sender); break;
            case "lobby":
                map.getLobby().teleport(sender); break;
            case "seekerlobby":
                map.getSeekerLobby().teleport(sender); break;
            case "exit":
                Config.exitPosition.teleport(sender); break;
            default:
                sender.sendMessage(Config.errorPrefix + Localization.message("COMMAND_INVALID_ARG").addAmount(args[1].toLowerCase()));
        }
    }

    public String getLabel() {
        return "goto";
    }

    public String getUsage() {
        return "<map> <spawn>";
    }

    public String getDescription() {
        return "Teleport to a map spawn zone";
    }

    public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        if(parameter.equals("map")) {
            return Maps.getAllMaps().stream().map(Map::getName).collect(Collectors.toList());
        } else if(parameter.equals("spawn")) {
            return Arrays.asList("spawn","lobby","seekerlobby","exit");
        }
        return null;
    }

}
