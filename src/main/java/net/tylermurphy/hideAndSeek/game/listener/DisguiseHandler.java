package net.tylermurphy.hideAndSeek.game.listener;

import static com.comphenix.protocol.PacketType.Play.Client.*;
import static net.tylermurphy.hideAndSeek.configuration.Config.solidifyTime;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.game.util.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.ArrayList;
import java.util.List;

public class DisguiseHandler implements Listener {

    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public DisguiseHandler(){
        protocolManager.addPacketListener(createProtocol());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        final Disguise disguise = Main.getInstance().getDisguiser().getDisguise(event.getPlayer());
        if(disguise == null) return;
        final Location lastLocation = event.getPlayer().getLocation();
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            final Location currentLocation = event.getPlayer().getLocation();
            if(lastLocation.getWorld() != currentLocation.getWorld()) return;
            double distance = lastLocation.distance(currentLocation);
            disguise.setSolidify(distance < .1);
        }, solidifyTime);
        if(event.getFrom().distance(event.getTo()) > .1)
            disguise.setSolidify(false);
    }

    private PacketAdapter createProtocol(){
        return new PacketAdapter(Main.getInstance(), USE_ENTITY) {

            @Override
            public void onPacketReceiving(PacketEvent event){
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                int id = packet.getIntegers().read(0);
                Disguise disguise = Main.getInstance().getDisguiser().getByEntityID(id);
                if(disguise == null) disguise = Main.getInstance().getDisguiser().getByHitBoxID(id);
                if(disguise == null) return;
                if(disguise.getPlayer().getGameMode() == GameMode.CREATIVE) return;
                event.setCancelled(true);
                handleAttack(disguise, player);
            }
        };
    }

    private final List<Player> debounce = new ArrayList<>();

    private void handleAttack(Disguise disguise, Player seeker){

        if(disguise.getPlayer() == seeker) return;

        double amount;
        if(Main.getInstance().supports(9)) {
            amount = seeker.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
        } else {
            return; //1.8 is not supported in Blockhunt yet!!!
        }

        disguise.setSolidify(false);
        if(debounce.contains(disguise.getPlayer())) return;
        debounce.add(disguise.getPlayer());
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            EntityDamageByEntityEvent event =
                    new EntityDamageByEntityEvent(seeker, disguise.getPlayer(), EntityDamageEvent.DamageCause.ENTITY_ATTACK, amount);
            event.setDamage(amount);
            disguise.getPlayer().setLastDamageCause(event);
            Main.getInstance().getServer().getPluginManager().callEvent(event);
            if(!event.isCancelled()){
                disguise.getPlayer().damage(amount);
                disguise.getPlayer().setVelocity(seeker.getLocation().getDirection().setY(.2).multiply(1));
            }

        }, 0);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> debounce.remove(disguise.getPlayer()), 10);
    }

}
