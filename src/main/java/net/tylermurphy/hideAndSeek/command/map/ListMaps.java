package net.tylermurphy.hideAndSeek.command.map;

import net.tylermurphy.hideAndSeek.command.util.Command;
import net.tylermurphy.hideAndSeek.configuration.Map;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

import static net.tylermurphy.hideAndSeek.configuration.Config.errorPrefix;
import static net.tylermurphy.hideAndSeek.configuration.Config.messagePrefix;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class ListMaps extends Command {

    public void execute(Player sender, String[] args) {
        Collection<Map> maps = Maps.getAllMaps();
        if(maps.size() < 1) {
            sender.sendMessage(errorPrefix + message("NO_MAPS"));
            return;
        }
        StringBuilder response = new StringBuilder(messagePrefix + message("LIST_MAPS"));
        for(Map map : maps) {
            response.append("\n    ").append(map.getName()).append(": ").append(map.isNotSetup() ? ChatColor.RED + "NOT SETUP" : ChatColor.GREEN + "SETUP").append(ChatColor.WHITE);
        }
        sender.sendMessage(response.toString());
    }

    public String getLabel() {
        return "list";
    }

    public String getUsage() {
        return "";
    }

    public String getDescription() {
        return "List all maps in the plugin";
    }

    public List<String> autoComplete(String parameter) {
        return null;
    }

}