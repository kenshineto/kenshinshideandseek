package net.tylermurphy.hideAndSeek.command;

import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.configuration.Map;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import net.tylermurphy.hideAndSeek.game.util.Status;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.errorPrefix;
import static net.tylermurphy.hideAndSeek.configuration.Config.messagePrefix;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class RemoveMap implements ICommand {

    public void execute(Player sender, String[] args) {
        if (Main.getInstance().getGame().getStatus() != Status.STANDBY) {
            sender.sendMessage(errorPrefix + message("GAME_INPROGRESS"));
            return;
        }
        Map map = Maps.getMap(args[0]);
        if(map == null) {
            sender.sendMessage(errorPrefix + message("INVALID_MAP"));
        } else if(!Maps.removeMap(args[0])){
            sender.sendMessage(errorPrefix + message("MAP_FAIL_DELETE").addAmount(args[0]));
        } else {
            sender.sendMessage(messagePrefix + message("MAP_DELETED").addAmount(args[0]));
        }
    }

    public String getLabel() {
        return "removemap";
    }

    public String getUsage() {
        return "<map>";
    }

    public String getDescription() {
        return "Remove a map from the plugin!";
    }

    public List<String> autoComplete(String parameter) {
        if(parameter != null && parameter.equals("map")) {
            return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
        }
        return null;
    }

}