package net.tylermurphy.hideAndSeek.command.world;

import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.command.util.ICommand;
import net.tylermurphy.hideAndSeek.util.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.tylermurphy.hideAndSeek.configuration.Config.errorPrefix;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Tp implements ICommand {
    public void execute(Player sender, String[] args) {
        Location test = new Location(args[0], 0, 0,0);
        if(!test.exists()) {
            sender.sendMessage(errorPrefix + message("WORLD_DOESNT_EXIT"));
            return;
        }
        World world = test.load();
        if(world == null) {
            sender.sendMessage(errorPrefix + message("WORLD_LOAD_FAILED"));
            return;
        }
        Location loc = new Location(world.getName(), world.getSpawnLocation());
        loc.teleport(sender);
    }

    public String getLabel() {
        return "tp";
    }

    public String getUsage() {
        return "<world>";
    }

    public String getDescription() {
        return "Teleport to another world";
    }

    public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        if(parameter.equals("world")) {
            return Main.getInstance().getWorlds();
        }
        return null;
    }
}
