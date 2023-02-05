package net.tylermurphy.hideAndSeek.command.world;

import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.command.util.ICommand;
import net.tylermurphy.hideAndSeek.util.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.tylermurphy.hideAndSeek.configuration.Config.errorPrefix;
import static net.tylermurphy.hideAndSeek.configuration.Config.messagePrefix;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Create implements ICommand {

    public void execute(Player sender, String[] args) {
        List<String> worlds = Main.getInstance().getWorlds();
        if(worlds.contains(args[0])) {
            sender.sendMessage(errorPrefix + message("WORLD_EXISTS").addAmount(args[0]));
            return;
        }
        WorldType type;
        World.Environment environment;
        switch (args[1]) {
            case "normal":
                type = WorldType.NORMAL;
                environment = World.Environment.NORMAL;
                break;
            case "flat":
                type = WorldType.FLAT;
                environment = World.Environment.NORMAL;
                break;
            case "nether":
                type = WorldType.NORMAL;
                environment = World.Environment.NETHER;
                break;
            case "end":
                type = WorldType.NORMAL;
                environment = World.Environment.THE_END;
                break;
            default:
                sender.sendMessage(errorPrefix + message("INVALID_WORLD_TYPE").addAmount(args[1]));
                return;
        }

        Location temp = new Location(args[0], 0, 0, 0);

        if (temp.load(type, environment) == null) {
            sender.sendMessage(errorPrefix + message("WORLD_ADDED_FAILED"));
        } else {
            sender.sendMessage(messagePrefix + message("WORLD_ADDED").addAmount(args[0]));
        }

    }

    public String getLabel() {
        return "create";
    }

    public String getUsage() {
        return "<name> <type>";
    }

    public String getDescription() {
        return "Create a new world";
    }

    public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        if(parameter.equals("name")) {
            return Collections.singletonList("name");
        }
        if(parameter.equals("type")) {
            return Arrays.asList("normal", "flat", "nether", "end");
        }
        return null;
    }
}