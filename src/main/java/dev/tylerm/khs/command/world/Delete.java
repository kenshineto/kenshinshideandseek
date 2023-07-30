package dev.tylerm.khs.command.world;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.command.Confirm;
import dev.tylerm.khs.command.util.ICommand;
import dev.tylerm.khs.configuration.Config;
import dev.tylerm.khs.configuration.Localization;
import dev.tylerm.khs.world.WorldLoader;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class Delete implements ICommand {

    public void execute(Player sender, String[] args) {
        java.util.List<String> worlds = Main.getInstance().getWorlds();
        if(!worlds.contains(args[0])) {
            sender.sendMessage(Config.errorPrefix + Localization.message("WORLD_DOESNT_EXIST").addAmount(args[0]));
            return;
        }

        Confirm.Confirmation confirmation = new Confirm.Confirmation(args[0], world -> {
            java.util.List<String> worlds_now = Main.getInstance().getWorlds();
            if(!worlds_now.contains(world)) {
                sender.sendMessage(Config.errorPrefix + Localization.message("WORLD_DOESNT_EXIST").addAmount(world));
                return;
            }
            World bukkit_world = Bukkit.getWorld(world);
            if(bukkit_world != null && bukkit_world.getPlayers().size() > 0) {
                sender.sendMessage(Config.errorPrefix + Localization.message("WORLD_NOT_EMPTY"));
                return;
            }
            String path = Main.getInstance().getWorldContainer().getPath() + File.separator + world;
            if (!Bukkit.getServer().unloadWorld(world, false)) {
                sender.sendMessage(Config.errorPrefix + Localization.message("WORLD_REMOVED_FAILED"));
                return;
            }
            try {
                WorldLoader.deleteDirectory(new File(path));
            } catch (Exception e) {
                sender.sendMessage(Config.errorPrefix + Localization.message("WORLD_REMOVED_FAILED"));
                return;
            }
            sender.sendMessage(Config.messagePrefix + Localization.message("WORLD_REMOVED").addAmount(world));
        });

        Confirm.confirmations.put(sender.getUniqueId(), confirmation);
        sender.sendMessage(Config.messagePrefix + Localization.message("CONFIRMATION"));

    }

    public String getLabel() {
        return "delete";
    }

    public String getUsage() {
        return "<name>";
    }

    public String getDescription() {
        return "Delete a world";
    }

    public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        if(parameter.equals("name")) {
            return Main.getInstance().getWorlds();
        }
        return null;
    }
}