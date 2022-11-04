package net.tylermurphy.hideAndSeek.command.map.blockhunt;

import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.command.util.Command;
import net.tylermurphy.hideAndSeek.configuration.Map;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import net.tylermurphy.hideAndSeek.game.util.Status;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.errorPrefix;
import static net.tylermurphy.hideAndSeek.configuration.Config.messagePrefix;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Enabled extends Command {

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
        boolean bool = Boolean.parseBoolean(args[1]);
        map.setBlockhunt(bool, map.getBlockHunt());
        Maps.setMap(map.getName(), map);
        sender.sendMessage(messagePrefix + message("BLOCKHUNT_SET_TO")
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
            return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
        }
        if(parameter.equals("bool")) {
            return Arrays.asList("true", "false");
        }
        return null;
    }

}
