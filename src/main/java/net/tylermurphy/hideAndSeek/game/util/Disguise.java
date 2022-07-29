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
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public class Disguise {

    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    final Player hider;
    final Material material;
    FallingBlock entity;
    Location solidLocation;
    boolean solid, solidify;

    public Disguise(Player player, Material material){
        this.hider = player;
        this.material = material;
        this.solid = false;
        respawnEntity();
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0,false, false));
    }

    public void remove(){
        if(entity != null)
            entity.remove();
        if(solid)
            sendBlockUpdate(Material.AIR);
        hider.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    @Nullable
    public Location getSolidLocation() {
        return solidLocation;
    }

    public int getEntityID() {
        if(entity == null) return -1;
        return entity.getEntityId();
    }

    public Player getPlayer() {
        return hider;
    }

    public boolean isSolid(){
        return solid;
    }

    public void update(){

        if(entity == null || entity.isDead()){
            if(entity != null) entity.remove();
            respawnEntity();
        }

        if(solidify){
            if(!solid)
                solidLocation = hider.getLocation().getBlock().getLocation();
            solid = true;
            sendBlockUpdate(material);
        } else if(solid){
            solid = false;
            sendBlockUpdate(Material.AIR);
        }
        sendToggleFallingBlock(!solid);
        sendFallingBlockUpdate();
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

    private void sendFallingBlockUpdate() {
        if(entity == null || entity.isDead()){
            if(entity != null) entity.remove();
            respawnEntity();
        }
        final PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        Location location = hider.getLocation();
        packet.getModifier().writeDefaults();
        packet.getIntegers().write(0, entity.getEntityId());
        if(solid){
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

    private void sendToggleFallingBlock(boolean show){
        Bukkit.getOnlinePlayers().forEach(receiver -> {
            if(receiver == hider) return;
            if(show)
                Main.getInstance().getEntityHider().showEntity(receiver, entity);
            else
                Main.getInstance().getEntityHider().hideEntity(receiver, entity);
        });
    }

    private void respawnEntity(){
        entity = hider.getLocation().getWorld().spawnFallingBlock(hider.getLocation(), material, (byte)0);
        entity.setGravity(false);
        entity.setDropItem(false);
    }

}