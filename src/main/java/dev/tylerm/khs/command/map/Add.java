package dev.tylerm.khs.command.map;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.command.util.ICommand;
import dev.tylerm.khs.configuration.Config;
import dev.tylerm.khs.configuration.Localization;
import dev.tylerm.khs.configuration.Map;
import dev.tylerm.khs.configuration.Maps;
import dev.tylerm.khs.game.util.Status;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class Add implements ICommand {

    public void execute(Player sender, String[] args) {
        if (Main.getInstance().getGame().getStatus() != Status.STANDBY) {
            sender.sendMessage(Config.errorPrefix + Localization.message("GAME_INPROGRESS"));
            return;
        }
        Map map = Maps.getMap(args[0]);
        if(map != null) {
            sender.sendMessage(Config.errorPrefix + Localization.message("MAP_ALREADY_EXISTS"));
        } else if(!args[0].matches("[a-zA-Z0-9]*") || args[0].length() < 1) {
            sender.sendMessage(Config.errorPrefix + Localization.message("INVALID_MAP_NAME"));
        } else {
            Maps.setMap(args[0], new Map(args[0]));
            sender.sendMessage(Config.messagePrefix + Localization.message("MAP_CREATED").addAmount(args[0]));
        }
    }

    public String getLabel() {
        return "add";
    }

    public String getUsage() {
        return "<name>";
    }

    public String getDescription() {
        return "Add a map to the plugin!";
    }

    public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        if(parameter.equals("name")) {
            return Collections.singletonList("name");
        }
        return null;
    }

}
