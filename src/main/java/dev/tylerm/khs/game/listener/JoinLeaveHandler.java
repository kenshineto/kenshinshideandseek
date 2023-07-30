package dev.tylerm.khs.game.listener;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.configuration.Items;
import dev.tylerm.khs.game.PlayerLoader;
import dev.tylerm.khs.game.util.Status;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import static dev.tylerm.khs.configuration.Config.*;
import static dev.tylerm.khs.configuration.Localization.message;

public class JoinLeaveHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(!Main.getInstance().getDatabase().getNameData().update(event.getPlayer().getUniqueId(), event.getPlayer().getName())){
            Main.getInstance().getLogger().warning("Failed to save name data for user: " + event.getPlayer().getName());
        }
        Main.getInstance().getBoard().remove(event.getPlayer());
        removeItems(event.getPlayer());
        if (Main.getInstance().getGame().checkCurrentMap()) return;
        if (autoJoin) {
            if (Main.getInstance().getGame().checkCurrentMap()) {
                event.getPlayer().sendMessage(errorPrefix + message("GAME_SETUP"));
                return;
            }
            Main.getInstance().getGame().join(event.getPlayer());
        } else if (teleportToExit) {
            if (
                    event.getPlayer().getWorld().getName().equals(Main.getInstance().getGame().getCurrentMap().getLobbyName()) ||
                    event.getPlayer().getWorld().getName().equals(Main.getInstance().getGame().getCurrentMap().getGameSpawnName())
            ) {
                exitPosition.teleport(event.getPlayer());
                event.getPlayer().setGameMode(GameMode.ADVENTURE);
            }
        } else {
            if (mapSaveEnabled && event.getPlayer().getWorld().getName().equals(Main.getInstance().getGame().getCurrentMap().getGameSpawnName())) {
                if (Main.getInstance().getGame().getStatus() != Status.STANDBY && Main.getInstance().getGame().getStatus() != Status.ENDING) {
                    Main.getInstance().getGame().join(event.getPlayer());
                } else {
                    exitPosition.teleport(event.getPlayer());
                    event.getPlayer().setGameMode(GameMode.ADVENTURE);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        handleLeave(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent event) {
        if(event.getReason().equals("Flying is not enabled on this server!")){
            event.setCancelled(true);
            return;
        }
        handleLeave(event.getPlayer());
    }

    private void handleLeave(Player player) {
        if(!Main.getInstance().getBoard().contains(player)) return;
        PlayerLoader.unloadPlayer(player);
        Main.getInstance().getBoard().remove(player);
        if(saveInventory) {
            ItemStack[] data = Main.getInstance().getDatabase().getInventoryData().getInventory(player.getUniqueId());
            player.getInventory().setContents(data);
        }
        if (Main.getInstance().getGame().getStatus() == Status.STANDBY) {
            Main.getInstance().getBoard().reloadLobbyBoards();
        } else {
            Main.getInstance().getBoard().reloadGameBoards();
        }
    }

    private void removeItems(Player player) {
        for(ItemStack si : Items.SEEKER_ITEMS) {
            if (si == null) continue;
            for (ItemStack i : player.getInventory().getContents())
                if (si.isSimilar(i)) player.getInventory().remove(i);
        }
        for(ItemStack hi : Items.HIDER_ITEMS) {
            if (hi == null) continue;
            for (ItemStack i : player.getInventory().getContents())
                if (hi.isSimilar(i)) player.getInventory().remove(i);
        }
    }

}
