package net.tylermurphy.hideAndSeek.command.map;

import net.tylermurphy.hideAndSeek.command.util.ICommand;
import net.tylermurphy.hideAndSeek.configuration.Map;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class GoTo implements ICommand {

    public void execute(Player sender, String[] args) {
        Map map = Maps.getMap(args[0]);
        if(map == null) {
            sender.sendMessage(errorPrefix + message("INVALID_MAP"));
            return;
        }
        if (map.isNotSetup()) {
            sender.sendMessage(errorPrefix + message("MAP_NOT_SETUP").addAmount(map.getName()));
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
                exitPosition.teleport(sender); break;
            default:
                sender.sendMessage(errorPrefix + message("COMMAND_INVALID_ARG").addAmount(args[1].toLowerCase()));
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
            return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
        } else if(parameter.equals("spawn")) {
            return Arrays.asList("spawn","lobby","seekerlobby","exit");
        }
        return null;
    }

}
