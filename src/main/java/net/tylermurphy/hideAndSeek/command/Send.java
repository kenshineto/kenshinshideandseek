package net.tylermurphy.hideAndSeek.command;

import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.command.util.ICommand;
import net.tylermurphy.hideAndSeek.configuration.Map;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import net.tylermurphy.hideAndSeek.game.util.Status;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.errorPrefix;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Send implements ICommand {

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

        if(map.isNotSetup()){
            sender.sendMessage(errorPrefix + message("MAP_NOT_SETUP"));
            return;
        }

        if (!Main.getInstance().getBoard().contains(sender)) {
            sender.sendMessage(errorPrefix + message("GAME_NOT_INGAME"));
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
            return Maps.getAllMaps().stream().filter(map -> !map.isNotSetup()).map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
        }
        return null;
    }

}