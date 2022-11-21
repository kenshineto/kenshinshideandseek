package net.tylermurphy.hideAndSeek.command.map.blockhunt.blocks;

import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.command.util.ICommand;
import net.tylermurphy.hideAndSeek.configuration.Map;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import net.tylermurphy.hideAndSeek.game.util.Status;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.errorPrefix;
import static net.tylermurphy.hideAndSeek.configuration.Config.messagePrefix;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Remove implements ICommand {

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
        Material block;
        try { block = Material.valueOf(args[1]); }
        catch (IllegalArgumentException e) {
            sender.sendMessage(errorPrefix + message("COMMAND_INVALID_ARG").addAmount(args[1]));
            return;
        }
        java.util.List<Material> blocks = map.getBlockHunt();
        if(!blocks.contains(block)) {
            sender.sendMessage(errorPrefix + message("BLOCKHUNT_BLOCK_DOESNT_EXIT").addAmount(args[1]));
        }
        blocks.remove(block);
        map.setBlockhunt(map.isBlockHuntEnabled(), blocks);
        Maps.setMap(map.getName(), map);
        sender.sendMessage(messagePrefix + message("BLOCKHUNT_BLOCK_REMOVED").addAmount(args[1]));
    }

    public String getLabel() {
        return "remove";
    }

    public String getUsage() {
        return "<map> <block>";
    }

    public String getDescription() {
        return "Remove a blockhunt block from a map!";
    }

    public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        if(parameter.equals("map")) {
            return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
        } else if(parameter.equals("block")) {
            return Arrays.stream(Material.values())
                    .filter(Material::isBlock)
                    .map(Material::toString)
                    .filter(s -> s.toUpperCase().startsWith(typed.toUpperCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }

}
