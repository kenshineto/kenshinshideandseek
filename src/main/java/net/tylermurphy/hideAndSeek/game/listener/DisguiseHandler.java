package net.tylermurphy.hideAndSeek.game.listener;

import static com.comphenix.protocol.PacketType.Play.Client.*;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.cryptomorin.xseries.XMaterial;
import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.game.util.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;

public class DisguiseHandler implements Listener {

    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private final PacketAdapter packetListener;

    public DisguiseHandler(){
        packetListener = createProtocol();
        protocolManager.addPacketListener(packetListener);
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
        }, 40L);
        if(event.getFrom().distance(event.getTo()) > .1)
            disguise.setSolidify(false);
    }

//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onInteract(PlayerInteractEvent event) {
//        Action action = event.getAction();
//        Player player = event.getPlayer();
//        Block block = event.
//    }

    private PacketAdapter createProtocol(){
        return new PacketAdapter(Main.getInstance(), USE_ENTITY) {

            @Override
            public void onPacketReceiving(PacketEvent event){
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
//                if(!Main.getInstance().getBoard().isSeeker(player)) return;
                int id = packet.getIntegers().read(0);
                Disguise disguise = Main.getInstance().getDisguiser().getByEntityID(id);
                if(disguise == null) disguise = Main.getInstance().getDisguiser().getByHitBoxID(id);
                if(disguise == null) return;
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
            amount = getItemDamageValue(seeker.getItemInHand(), disguise.getPlayer(), seeker);
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

    private int getItemDamageValue(ItemStack is, Player damaged, Player attacker) {
        double damageValue = 0;
        if (is != null) {
            if (is.getType() == XMaterial.WOODEN_SWORD.parseMaterial()) {
                damageValue = 5;
            } else if (is.getType() == Material.STONE_SWORD) {
                damageValue = 6;
            } else if (is.getType() == Material.IRON_SWORD) {
                damageValue = 7;
            } else if (is.getType() == Material.DIAMOND_SWORD) {
                damageValue = 8;
            } else {
                damageValue = 1;
            }
            damageValue += is.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
        }

        if (damaged != null) {
            Inventory i = damaged.getInventory();
            Material helmet = i.getItem(39).getType();
            Material chestplate = i.getItem(40).getType();
            Material leggings = i.getItem(41).getType();
            Material boots = i.getItem(42).getType();
            if (helmet == Material.LEATHER_HELMET)
                damageValue -= (0.5 / 1.5);
                // value shown at bar above the health bar / 1.5
            else if (helmet == Material.CHAINMAIL_HELMET
                    || helmet == Material.IRON_HELMET
                    || helmet == Material.DIAMOND_HELMET
                    || helmet == XMaterial.GOLDEN_HELMET.parseMaterial())
                damageValue -= (1 / 1.5);

            if (chestplate == Material.LEATHER_CHESTPLATE)
                damageValue -= (1.0);
            else if (chestplate == Material.CHAINMAIL_CHESTPLATE
                    || chestplate == XMaterial.GOLDEN_CHESTPLATE.parseMaterial())
                damageValue -= (2.5 / 1.5);
            else if (chestplate == Material.IRON_CHESTPLATE)
                damageValue -= (3 / 1.5);
            else if (chestplate == Material.DIAMOND_CHESTPLATE)
                damageValue -= (4 / 1.5);

            if (leggings == Material.LEATHER_LEGGINGS)
                damageValue -= (1 / 1.5);
            else if (leggings == XMaterial.GOLDEN_LEGGINGS.parseMaterial())
                damageValue -= (1.0);
            else if (leggings == Material.CHAINMAIL_LEGGINGS)
                damageValue -= (2 / 1.5);
            else if (leggings == Material.IRON_LEGGINGS)
                damageValue -= (2.5 / 1.5);
            else if (leggings == Material.DIAMOND_LEGGINGS)
                damageValue -= (3 / 1.5);

            if (boots == Material.LEATHER_BOOTS
                    || boots == XMaterial.GOLDEN_BOOTS.parseMaterial()
                    || boots == Material.CHAINMAIL_BOOTS)
                damageValue -= (0.5 / 1.5);
            else if (boots == Material.IRON_BOOTS)
                damageValue -= (1 / 1.5);
            else if (boots == Material.DIAMOND_BOOTS)
                damageValue -= (1.0);
        }

        for (PotionEffect effect : attacker.getActivePotionEffects()){
            if (effect.getType() == PotionEffectType.HARM) {
                damageValue += effect.getAmplifier()*1.5;
            }
        }

        return (int) Math.round(Math.max(damageValue, 0.0));
    }

}
