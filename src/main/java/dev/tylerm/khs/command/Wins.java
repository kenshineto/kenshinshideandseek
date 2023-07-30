package dev.tylerm.khs.command;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.command.util.ICommand;
import dev.tylerm.khs.configuration.Config;
import dev.tylerm.khs.configuration.Localization;
import dev.tylerm.khs.database.util.PlayerInfo;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Wins implements ICommand {

    public void execute(Player sender, String[] args) {
        Main.getInstance().getServer().getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {

            UUID uuid;
            String name;
            if (args.length == 0) {
                uuid = sender.getUniqueId();
                name = sender.getName();
            }
            else {
                name = args[0];
                uuid = Main.getInstance().getDatabase().getNameData().getUUID(args[0]);
            }
            if(uuid == null){
                sender.sendMessage(Config.errorPrefix + Localization.message("START_INVALID_NAME").addPlayer(args[0]));
                return;
            }
            PlayerInfo info = Main.getInstance().getDatabase().getGameData().getInfo(uuid);
            if (info == null) {
                sender.sendMessage(Config.errorPrefix + Localization.message("NO_GAME_INFO"));
                return;
            }
            String message = ChatColor.WHITE + "" + ChatColor.BOLD + "==============================\n";
            message = message + Localization.message("INFORMATION_FOR").addPlayer(name) + "\n";
            message = message + "==============================\n";
            message = message + String.format("%sTOTAL WINS: %s%s\n%sHIDER WINS: %s%s\n%sSEEKER WINS: %s%s\n%sGAMES PLAYED: %s",
                    ChatColor.YELLOW, ChatColor.WHITE, info.getSeekerWins() +info.getHiderWins(), ChatColor.GOLD, ChatColor.WHITE, info.getHiderWins(),
                    ChatColor.RED, ChatColor.WHITE, info.getSeekerWins(), ChatColor.WHITE, info.getSeekerGames() +info.getHiderGames());
            message = message + ChatColor.WHITE + "" + ChatColor.BOLD + "\n==============================";
            sender.sendMessage(message);

        });
    }

    public String getLabel() {
        return "wins";
    }

    public String getUsage() {
        return "<*player>";
    }

    public String getDescription() {
        return "Get the win information for yourself or another player.";
    }

    public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        return Collections.singletonList(parameter);
    }
}
