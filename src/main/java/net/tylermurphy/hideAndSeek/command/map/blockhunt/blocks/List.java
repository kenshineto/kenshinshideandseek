package net.tylermurphy.hideAndSeek.command.map.blockhunt.blocks;

import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.command.util.ICommand;
import net.tylermurphy.hideAndSeek.configuration.Map;
import net.tylermurphy.hideAndSeek.configuration.Maps;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.errorPrefix;
import static net.tylermurphy.hideAndSeek.configuration.Config.messagePrefix;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class List implements ICommand {

    public void execute(Player sender, String[] args) {
        if (!Main.getInstance().supports(9)) {
            sender.sendMessage(errorPrefix + message("BLOCKHUNT_UNSUPPORTED"));
            return;
        }
        Map map = Maps.getMap(args[0]);
        if(map == null) {
            sender.sendMessage(errorPrefix + message("INVALID_MAP"));
            return;
        }
        java.util.List<Material> blocks = map.getBlockHunt();
        if(blocks.isEmpty()) {
            sender.sendMessage(errorPrefix + message("NO_BLOCKS"));
            return;
        }
        StringBuilder response = new StringBuilder(messagePrefix + message("BLOCKHUNT_LIST_BLOCKS"));
        for(int i = 0; i < blocks.size(); i++) {
            response.append(String.format("\n%s. %s", i, blocks.get(i).toString()));
        }
        sender.sendMessage(response.toString());
    }

    public String getLabel() {
        return "list";
    }

    public String getUsage() {
        return "<map>";
    }

    public String getDescription() {
        return "List all blockhunt blocks in a map";
    }

    public java.util.List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        if(parameter.equals("map")) {
            return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
        }
        return null;
    }
}
