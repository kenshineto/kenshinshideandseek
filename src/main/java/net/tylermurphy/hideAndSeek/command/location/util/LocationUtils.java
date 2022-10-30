package net.tylermurphy.hideAndSeek.command.location.util;

import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.configuration.Map;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import net.tylermurphy.hideAndSeek.game.util.Status;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

/**
 * @author bobby29831
 */
public class LocationUtils {

    public static void setLocation(@NotNull Player player, @NotNull Locations place, String mapName, @NotNull Consumer<Map> consumer) {

        if (Main.getInstance().getGame().getStatus() != Status.STANDBY) {
            player.sendMessage(errorPrefix + message("GAME_INPROGRESS"));
            return;
        }

        if (player.getLocation().getBlockX() == 0 || player.getLocation().getBlockZ() == 0 || player.getLocation().getBlockY() == 0){
            player.sendMessage(errorPrefix + message("NOT_AT_ZERO"));
            return;
        }

        Map map = null;
        if(mapName != null) {
            map = Maps.getMap(mapName);
            if (map == null) {
                player.sendMessage(errorPrefix + message("INVALID_MAP"));
                return;
            }
        }

        consumer.accept(map);
        if(map != null)
            Maps.setMap(mapName, map);
        player.sendMessage(messagePrefix + message(place.message()));
    }

}