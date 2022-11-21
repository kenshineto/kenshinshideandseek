package net.tylermurphy.hideAndSeek.command.world;

import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.command.util.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.tylermurphy.hideAndSeek.configuration.Config.errorPrefix;
import static net.tylermurphy.hideAndSeek.configuration.Config.messagePrefix;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class List implements ICommand {

    public void execute(Player sender, String[] args) {
        java.util.List<String> worlds = Main.getInstance().getWorlds();
        if(worlds.isEmpty()) {
            sender.sendMessage(errorPrefix + message("NO_WORLDS"));
        } else {
            StringBuilder response = new StringBuilder(messagePrefix + message("LIST_WORLDS"));
            for (String world : worlds) {
                boolean loaded = Bukkit.getWorld(world) != null;
                response.append("\n    ").append(world).append(": ").append(loaded ? ChatColor.GREEN + "LOADED" : ChatColor.YELLOW + "NOT LOADED").append(ChatColor.WHITE);
            }
            sender.sendMessage(response.toString());
        }
    }

    public String getLabel() {
        return "list";
    }

    public String getUsage() {
        return "";
    }

    public String getDescription() {
        return "List all worlds in the server";
    }

    public java.util.List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        return null;
    }

}
