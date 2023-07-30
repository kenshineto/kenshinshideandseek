package dev.tylerm.khs.command;

import dev.tylerm.khs.command.util.ICommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

import static dev.tylerm.khs.configuration.Config.errorPrefix;
import static dev.tylerm.khs.configuration.Localization.message;

public class Confirm implements ICommand {

    public static final Map<UUID, Confirmation> confirmations = new HashMap<>();

    public void execute(Player sender, String[] args) {
        Confirmation confirmation = confirmations.get(sender.getUniqueId());
        confirmations.remove(sender.getUniqueId());
        if(confirmation == null) {
            sender.sendMessage(errorPrefix + message("NO_CONFIRMATION"));
        } else {
            long now = System.currentTimeMillis();
            float secs = (now - confirmation.start) / 1000F;
            if(secs > 10) {
                sender.sendMessage(errorPrefix + message("CONFIRMATION_TIMED_OUT"));
                return;
            }
            confirmation.callback.accept(confirmation.data);
        }
    }

    public String getLabel() {
        return "confirm";
    }

    public String getUsage() {
        return "";
    }

    public String getDescription() {
        return "Confirm another command if required";
    }

    public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        return null;
    }

    public static class Confirmation {
        public final Consumer<String> callback;
        public final String data;
        public final long start;

        public Confirmation(String data, Consumer<String> callback) {
            this.callback = callback;
            this.data = data;
            this.start = System.currentTimeMillis();
        }

    }

}
