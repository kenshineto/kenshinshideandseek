package net.tylermurphy.hideAndSeek.game.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import net.tylermurphy.hideAndSeek.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.InvocationTargetException;

public class Disguise {

    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    final Player hider;
    final Material material;
    FallingBlock block;
    Horse hitBox;
    Location solidLocation;
    boolean solid, solidify;
    static Team hidden;

    static {
        if(Main.getInstance().supports(9)) {
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
            hidden = board.getTeam("KenshinHideAndSeek_CollisionGroup");
            if (hidden == null) {
                hidden = board.registerNewTeam("KenshinHideAndSeek_CollisionGroup");
            }
            hidden.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            hidden.setCanSeeFriendlyInvisibles(false);
        }
    }

    public Disguise(Player player, Material material){
        this.hider = player;
        this.material = material;
        this.solid = false;
        respawnFallingBlock();
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0,false, false));
        if(Main.getInstance().supports(9)) {
            hidden.addEntry(player.getName());
        } else {
            hider.spigot().setCollidesWithEntities(false);
        }
    }

    public void remove(){
        if(block != null)
            block.remove();
        if(hitBox != null){
            if(Main.getInstance().supports(9))
                hidden.removeEntry(hitBox.getUniqueId().toString());
            hitBox.remove();
        }
        if(solid)
            sendBlockUpdate(Material.AIR);
        hider.removePotionEffect(PotionEffectType.INVISIBILITY);
        if(Main.getInstance().supports(9)) {
            hidden.removeEntry(hider.getName());
        } else {
            hider.spigot().setCollidesWithEntities(true);
        }
    }

    public int getEntityID() {
        if(block == null) return -1;
        return block.getEntityId();
    }

    public int getHitBoxID() {
        if(hitBox == null) return -1;
        return hitBox.getEntityId();
    }

    public Player getPlayer() {
        return hider;
    }

    public void update(){

        if(block == null || block.isDead()){
            if(block != null) block.remove();
            respawnFallingBlock();
        }

        if(solidify){
            if(!solid) {
                solid = true;
                solidLocation = hider.getLocation().getBlock().getLocation();
                respawnHotbox();
                teleportEntity(hitBox, false);
            }
            sendBlockUpdate(material);
        } else if(solid){
            solid = false;
            if(Main.getInstance().supports(9))
                hidden.removeEntry(hitBox.getUniqueId().toString());
            hitBox.remove();
            hitBox = null;
            sendBlockUpdate(Material.AIR);
        }
        toggleEntityVisibility(block, !solid);
        teleportEntity(block, solid);
    }

    public void setSolidify(boolean value){
        this.solidify = value;
    }

    private void sendBlockUpdate(Material material){
        final PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
        packet.getModifier().writeDefaults();
        packet.getBlockPositionModifier().write(0, new BlockPosition(solidLocation.toVector()));
        packet.getBlockData().write(0, WrappedBlockData.createData(material));
        Bukkit.getOnlinePlayers().forEach(receiver -> {
            if(receiver == hider) return;
            try {
                protocolManager.sendServerPacket(receiver, packet);
            } catch (InvocationTargetException ignored) {}
        });
    }

    private void teleportEntity(Entity entity, boolean center) {
        final PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        Location location = hider.getLocation();
        packet.getModifier().writeDefaults();
        packet.getIntegers().write(0, entity.getEntityId());
        if(center){
            packet.getDoubles().write(0, Math.round(location.getX()+.5)-.5);
            packet.getDoubles().write(1, (double)Math.round(location.getY()));
            packet.getDoubles().write(2, Math.round(location.getZ()+.5)-.5);
        } else {
            packet.getDoubles().write(0, location.getX());
            packet.getDoubles().write(1, location.getY());
            packet.getDoubles().write(2, location.getZ());
        }
        Bukkit.getOnlinePlayers().forEach(receiver -> {
            try {
                protocolManager.sendServerPacket(receiver, packet);
            } catch (InvocationTargetException ignored) {}
        });
    }

    private void toggleEntityVisibility(Entity entity, boolean show){
        if(entity == null) return;
        Bukkit.getOnlinePlayers().forEach(receiver -> {
            if(receiver == hider) return;
            if(show)
                Main.getInstance().getEntityHider().showEntity(receiver, entity);
            else
                Main.getInstance().getEntityHider().hideEntity(receiver, entity);
        });
    }

    private void respawnFallingBlock(){
        block = hider.getLocation().getWorld().spawnFallingBlock(hider.getLocation(), material, (byte)0);
        block.setGravity(false);
        block.setDropItem(false);
        block.setInvulnerable(true);
    }

    private void respawnHotbox(){
        hitBox = (Horse) hider.getLocation().getWorld().spawnEntity(hider.getLocation().add(0, 1000, 0), EntityType.HORSE);
        hitBox.setAI(false);
        hitBox.setGravity(false);
        hitBox.setInvulnerable(true);
        hitBox.setCanPickupItems(false);
        hitBox.setCollidable(false);
        hitBox.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0,false, false));
        if(Main.getInstance().supports(9)){
            hidden.addEntry(hitBox.getUniqueId().toString());
        }
    }

}