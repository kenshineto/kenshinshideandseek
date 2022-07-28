package net.tylermurphy.hideAndSeek.game.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import net.tylermurphy.hideAndSeek.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DisguiseHandler implements Listener {

    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    private final Map<UUID,Location> locations = new HashMap<>();
    private final Map<UUID,Long> times = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        checkStandingStill(event.getPlayer());
        FallingBlock block = Main.getInstance().getDisguiser().getBlock(event.getPlayer());
        if(block == null) return;
        UUID uuid = event.getPlayer().getUniqueId();
        boolean finalFixLocation = times.containsKey(uuid) && new Date().getTime()-times.get(uuid) > 1000;
        Bukkit.getOnlinePlayers().forEach(player -> {
            teleportEntity(player, block, event.getPlayer().getLocation(), finalFixLocation);
        });
    }

    private void checkStandingStill(Player player){
        UUID uuid = player.getUniqueId();
        Location lastLoc = locations.get(uuid);
        Location currentLoc = player.getLocation();
        if(lastLoc == null) lastLoc = currentLoc;
        double distance = lastLoc.distance(currentLoc);
        if(distance < .05){
            if(!times.containsKey(uuid))
                times.put(uuid, new Date().getTime());
        } else {
            times.remove(uuid);
        }
        locations.put(uuid, currentLoc);
    }

    private void teleportEntity(Player player, FallingBlock block, Location location, boolean fixLocation) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packet.getModifier().writeDefaults();
        packet.getIntegers().write(0, block.getEntityId());
        if(fixLocation){
            packet.getDoubles().write(0, Math.round(location.getX()+.5)-.5);
            packet.getDoubles().write(1, (double)Math.round(location.getY()));
            packet.getDoubles().write(2, Math.round(location.getZ()+.5)-.5);
        } else {
            packet.getDoubles().write(0, location.getX());
            packet.getDoubles().write(1, location.getY());
            packet.getDoubles().write(2, location.getZ());
        }

        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
