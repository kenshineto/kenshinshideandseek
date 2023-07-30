package dev.tylerm.khs.command;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.command.util.ICommand;
import dev.tylerm.khs.configuration.Config;
import dev.tylerm.khs.configuration.Localization;
import dev.tylerm.khs.configuration.Map;
import dev.tylerm.khs.configuration.Maps;
import dev.tylerm.khs.game.util.Status;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class Send implements ICommand {

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

        if(map.isNotSetup()){
            sender.sendMessage(Config.errorPrefix + Localization.message("MAP_NOT_SETUP"));
            return;
        }

        if (!Main.getInstance().getBoard().contains(sender)) {
            sender.sendMessage(Config.errorPrefix + Localization.message("GAME_NOT_INGAME"));
            return;
        }

        Main.getInstance().getGame().setCurrentMap(map);
        Main.getInstance().getBoard().reloadLobbyBoards();
        for(Player player : Main.getInstance().getBoard().getPlayers()) {
            map.getLobby().teleport(player);
        }

    }

    public String getLabel() {
        return "send";
    }

    public String getUsage() {
        return "<map>";
    }

    public String getDescription() {
        return "Set the current lobby to another map";
    }

    public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        if(parameter.equals("map")) {
            return Maps.getAllMaps().stream().filter(map -> !map.isNotSetup()).map(Map::getName).collect(Collectors.toList());
        }
        return null;
    }

}