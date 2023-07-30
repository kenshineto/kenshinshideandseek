package dev.tylerm.khs.command.map.blockhunt;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.command.util.ICommand;
import dev.tylerm.khs.configuration.Config;
import dev.tylerm.khs.configuration.Localization;
import dev.tylerm.khs.configuration.Map;
import dev.tylerm.khs.configuration.Maps;
import dev.tylerm.khs.game.util.Status;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Enabled implements ICommand {

    public void execute(Player sender, String[] args) {
        if (!Main.getInstance().supports(9)) {
            sender.sendMessage(Config.errorPrefix + Localization.message("BLOCKHUNT_UNSUPPORTED"));
            return;
        }
        if (Main.getInstance().getGame().getStatus() != Status.STANDBY) {
            sender.sendMessage(Config.errorPrefix + Localization.message("GAME_INPROGRESS"));
            return;
        }
        Map map = Maps.getMap(args[0]);
        if(map == null) {
            sender.sendMessage(Config.errorPrefix + Localization.message("INVALID_MAP"));
            return;
        }
        boolean bool = Boolean.parseBoolean(args[1]);
        map.setBlockhunt(bool, map.getBlockHunt());
        Maps.setMap(map.getName(), map);
        sender.sendMessage(Config.messagePrefix + Localization.message("BLOCKHUNT_SET_TO")
                .addAmount(bool ? ChatColor.GREEN + "true" : ChatColor.RED + "false") + ChatColor.WHITE);
    }

    public String getLabel() {
        return "enabled";
    }

    public String getUsage() {
        return "<map> <bool>";
    }

    public String getDescription() {
        return "Sets blockhunt enabled or disabled in a current map";
    }

    public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        if(parameter.equals("map")) {
            return Maps.getAllMaps().stream().map(Map::getName).collect(Collectors.toList());
        }
        if(parameter.equals("bool")) {
            return Arrays.asList("true", "false");
        }
        return null;
    }

}
