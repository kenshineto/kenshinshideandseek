package dev.tylerm.khs.command;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.command.util.ICommand;
import dev.tylerm.khs.database.util.PlayerInfo;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static dev.tylerm.khs.configuration.Config.errorPrefix;
import static dev.tylerm.khs.configuration.Localization.message;

public class Top implements ICommand {

    public void execute(Player sender, String[] args) {
        int page;
        if (args.length == 0) page = 1;
        else try{
            page = Integer.parseInt(args[0]);
        } catch(Exception e) {
            sender.sendMessage(errorPrefix + message("WORLDBORDER_INVALID_INPUT").addAmount(args[0]));
            return;
        }
        if (page < 1) {
            sender.sendMessage(errorPrefix + message("WORLDBORDER_INVALID_INPUT").addAmount(page));
            return;
        }
        StringBuilder message = new StringBuilder(String.format(
                "%s------- %sLEADERBOARD %s(Page %s) %s-------\n",
                ChatColor.WHITE, ChatColor.BOLD, ChatColor.GRAY, page, ChatColor.WHITE));
        List<PlayerInfo> infos = Main.getInstance().getDatabase().getGameData().getInfoPage(page);
        int i = 1 + (page-1)*10;
        if (infos == null) {
            sender.sendMessage(errorPrefix + message("NO_GAME_INFO"));
            return;
        }
        for(PlayerInfo info : infos) {
            String name = Main.getInstance().getDatabase().getNameData().getName(info.getUniqueId());
            ChatColor color;
            switch (i) {
                case 1: color = ChatColor.YELLOW; break;
                case 2: color = ChatColor.GRAY; break;
                case 3: color = ChatColor.GOLD; break;
                default: color = ChatColor.WHITE; break;
            }
            message.append(String.format("%s%s. %s%s %s%s\n",
                    color, i, ChatColor.RED, info.getSeekerWins() +info.getHiderWins(), ChatColor.WHITE, name));
            i++;
        }
        sender.sendMessage(message.toString());
    }

    public String getLabel() {
        return "top";
    }

    public String getUsage() {
        return "<*page>";
    }

    public String getDescription() {
        return "Gets the top players in the server.";
    }

    public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        return Collections.singletonList(parameter);
    }

}
