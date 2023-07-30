package dev.tylerm.khs.command.world;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.configuration.Config;
import dev.tylerm.khs.configuration.Localization;
import dev.tylerm.khs.util.Location;
import dev.tylerm.khs.command.util.ICommand;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Create implements ICommand {

    public void execute(Player sender, String[] args) {
        List<String> worlds = Main.getInstance().getWorlds();
        if(worlds.contains(args[0])) {
            sender.sendMessage(Config.errorPrefix + Localization.message("WORLD_EXISTS").addAmount(args[0]));
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
                sender.sendMessage(Config.errorPrefix + Localization.message("INVALID_WORLD_TYPE").addAmount(args[1]));
                return;
        }

        Location temp = new Location(args[0], 0, 0, 0);

        if (temp.load(type, environment) == null) {
            sender.sendMessage(Config.errorPrefix + Localization.message("WORLD_ADDED_FAILED"));
        } else {
            sender.sendMessage(Config.messagePrefix + Localization.message("WORLD_ADDED").addAmount(args[0]));
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