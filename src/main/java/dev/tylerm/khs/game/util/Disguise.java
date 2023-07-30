package dev.tylerm.khs.game.util;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.ActionBar;
import dev.tylerm.khs.util.packet.BlockChangePacket;
import dev.tylerm.khs.util.packet.EntityTeleportPacket;
import dev.tylerm.khs.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

@SuppressWarnings("deprecation")
public class Disguise {

    final Player hider;
    final Material material;
    FallingBlock block;
    AbstractHorse hitBox;
    Location blockLocation;
    boolean solid, solidify, solidifying;
    static Team hidden;

    static {
        if(Main.getInstance().supports(9)) {
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
            hidden = board.getTeam("KHS_Collision");
            if (hidden == null) {
                hidden = board.registerNewTeam("KHS_Collision");
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
            sendBlockUpdate(blockLocation, Material.AIR);
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
                blockLocation = hider.getLocation().getBlock().getLocation();
                respawnHitbox();
            }
            sendBlockUpdate(blockLocation, material);
        } else if(solid){
            solid = false;
            if(Main.getInstance().supports(9))
                hidden.removeEntry(hitBox.getUniqueId().toString());
            hitBox.remove();
            hitBox = null;
            sendBlockUpdate(blockLocation, Material.AIR);
        }
        toggleEntityVisibility(block, !solid);
        teleportEntity(hitBox, true);
        teleportEntity(block, solid);
    }

    public void setSolidify(boolean value){
        this.solidify = value;
    }

    private void sendBlockUpdate(Location location, Material material){
        BlockChangePacket packet = new BlockChangePacket();
        packet.setBlockPosition(location);
        packet.setMaterial(material);
        Bukkit.getOnlinePlayers().forEach(receiver -> {
            if(receiver.getName().equals(hider.getName())) return;
            packet.send(receiver);
        });
    }

    private void teleportEntity(Entity entity, boolean center) {
        if(entity == null) return;
        EntityTeleportPacket packet = new EntityTeleportPacket();
        packet.setEntity(entity);
        double x,y,z;
        if(center){
            x = Math.round(hider.getLocation().getX()+.5)-.5;
            y = Math.round(hider.getLocation().getY());
            z = Math.round(hider.getLocation().getZ()+.5)-.5;
        } else {
            x = hider.getLocation().getX();
            y = hider.getLocation().getY();
            z = hider.getLocation().getZ();
        }
        packet.setX(x);
        packet.setY(y);
        packet.setZ(z);
        Bukkit.getOnlinePlayers().forEach(packet::send);
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
        block = hider.getLocation().getWorld().spawnFallingBlock(hider.getLocation().add(0, 1000, 0), material, (byte)0);
        if (Main.getInstance().supports(10)) {
            block.setGravity(false);
        }
        block.setDropItem(false);
        block.setInvulnerable(true);
    }

    private void respawnHitbox(){
        if (Main.getInstance().supports(11)) {
            hitBox = (AbstractHorse) hider.getLocation().getWorld().spawnEntity(hider.getLocation().add(0, 1000, 0), EntityType.SKELETON_HORSE);
        } else {
            hitBox = (AbstractHorse) hider.getLocation().getWorld().spawnEntity(hider.getLocation().add(0, 1000, 0), EntityType.HORSE);
            hitBox.setVariant(Horse.Variant.SKELETON_HORSE);
        }
        if (Main.getInstance().supports(10)) {
            hitBox.setGravity(false);
        }
        hitBox.setAI(false);
        hitBox.setInvulnerable(true);
        hitBox.setCanPickupItems(false);
        hitBox.setCollidable(false);
        hitBox.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0,false, false));
        if(Main.getInstance().supports(9)){
            hidden.addEntry(hitBox.getUniqueId().toString());
        }
    }

    public void startSolidifying() {
        if (solidifying) return;
        if (solid) return;
        solidifying = true;
        final Location lastLocation = hider.getLocation();
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> solidifyUpdate(lastLocation, 3), 10);
    }

    private void solidifyUpdate(Location lastLocation, int time) {
        Location currentLocation = hider.getLocation();
        if(lastLocation.getWorld() != currentLocation.getWorld()) {
            solidifying = false;
            return;
        }
        if(lastLocation.distance(currentLocation) > .1) {
            solidifying = false;
            return;
        }
        if(time == 0) {
            ActionBar.clearActionBar(hider);
            setSolidify(true);
            solidifying = false;
        } else {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < time; i++) {
                s.append("▪");
            }
            ActionBar.sendActionBar(hider, s.toString());
            XSound.BLOCK_NOTE_BLOCK_PLING.play(hider, 1, 1);
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> solidifyUpdate(lastLocation, time - 1), 20);
        }
    }

}