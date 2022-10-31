package net.tylermurphy.hideAndSeek.command;

import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.configuration.Map;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import net.tylermurphy.hideAndSeek.game.util.Status;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.tylermurphy.hideAndSeek.configuration.Config.errorPrefix;
import static net.tylermurphy.hideAndSeek.configuration.Config.messagePrefix;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class AddMap implements ICommand {

    public void execute(Player sender, String[] args) {
        if (Main.getInstance().getGame().getStatus() != Status.STANDBY) {
            sender.sendMessage(errorPrefix + message("GAME_INPROGRESS"));
            return;
        }
        Map map = Maps.getMap(args[0]);
        if(map != null) {
            sender.sendMessage(errorPrefix + message("MAP_ALREADY_EXISTS"));
        } else if(!args[0].matches("[a-zA-Z0-9]*") || args[0].length() < 1) {
            sender.sendMessage(errorPrefix + message("INVALID_MAP_NAME"));
        } else {
            Maps.setMap(args[0], new Map(args[0]));
            sender.sendMessage(messagePrefix + message("MAP_CREATED").addAmount(args[0]));
        }
    }

    public String getLabel() {
        return "addmap";
    }

    public String getUsage() {
        return "<name>";
    }

    public String getDescription() {
        return "Add a map to the plugin!";
    }

    public List<String> autoComplete(String parameter) {
        if(parameter != null && parameter.equals("name")) {
            return Collections.singletonList("name");
        }
        return null;
    }

}
