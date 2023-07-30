package dev.tylerm.khs.game.listener;

import com.google.common.collect.Sets;
import dev.tylerm.khs.game.listener.events.PlayerJumpEvent;
import dev.tylerm.khs.Main;
import dev.tylerm.khs.configuration.Map;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Set;
import java.util.UUID;

public class MovementHandler implements Listener {

    private final Set<UUID> prevPlayersOnGround = Sets.newHashSet();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {

        if (event.getTo() == null || event.getTo().getWorld() == null) return;
        checkJumping(event);
        checkBounds(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJump(PlayerJumpEvent event) {
        if(Main.getInstance().getBoard().isSpectator(event.getPlayer()) && event.getPlayer().getAllowFlight()) {
            event.getPlayer().setFlying(true);
        }
    }

    private void checkJumping(PlayerMoveEvent event){
        if (event.getPlayer().getVelocity().getY() > 0) {
            if (event.getPlayer().getLocation().getBlock().getType() != Material.LADDER && prevPlayersOnGround.contains(event.getPlayer().getUniqueId())) {
                if (!event.getPlayer().isOnGround()) {
                    Main.getInstance().getServer().getPluginManager().callEvent(new PlayerJumpEvent(event.getPlayer()));
                }
            }
        }
        if (event.getPlayer().isOnGround()) {
            prevPlayersOnGround.add(event.getPlayer().getUniqueId());
        } else {
            prevPlayersOnGround.remove(event.getPlayer().getUniqueId());
        }
    }

    private void checkBounds(PlayerMoveEvent event){
        if (!Main.getInstance().getBoard().contains(event.getPlayer())) return;
        if (!event.getPlayer().getWorld().getName().equals(Main.getInstance().getGame().getCurrentMap().getGameSpawnName())) return;
        if (!event.getTo().getWorld().getName().equals(Main.getInstance().getGame().getCurrentMap().getGameSpawnName())) return;
        if (event.getPlayer().hasPermission("hs.leavebounds")) return;
        Map map = Main.getInstance().getGame().getCurrentMap();
        if (event.getTo().getBlockX() < map.getBoundsMin().getBlockX() || event.getTo().getBlockX() > map.getBoundsMax().getBlockX() || event.getTo().getBlockZ() < map.getBoundsMin().getZ() || event.getTo().getBlockZ() > map.getBoundsMax().getZ()) {
            event.setCancelled(true);
        }
    }

}
