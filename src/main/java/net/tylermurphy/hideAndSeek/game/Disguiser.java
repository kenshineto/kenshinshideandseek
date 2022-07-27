package net.tylermurphy.hideAndSeek.game;

import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class Disguiser {

    private final Map<Player, FallingBlock> blocks;

    public Disguiser(){
        this.blocks = new HashMap<>();
    }

    public FallingBlock getBlock(Player player){
        return blocks.get(player);
    }

    public boolean contains(FallingBlock block) { return blocks.containsValue(block); }

    public boolean disguised(Player player) { return blocks.containsKey(player); }

    public void check(){
        for(Map.Entry<Player, FallingBlock> set : blocks.entrySet()){
            Player player = set.getKey();
            FallingBlock block = set.getValue();
            if(block.isDead()){
                block.remove();
                FallingBlock replacement = player.getLocation().getWorld().spawnFallingBlock(player.getLocation(), block.getMaterial(), (byte)0);
                replacement.setGravity(false);
                replacement.setDropItem(false);
                blocks.put(player, replacement);
            }
        }
    }

    public void disguise(Player player, Material material){
        if(blocks.containsKey(player)){
            FallingBlock block = blocks.get(player);
            block.remove();
        }
        FallingBlock block = player.getLocation().getWorld().spawnFallingBlock(player.getLocation(), material, (byte)0);
        block.setGravity(false);
        block.setDropItem(false);
        blocks.put(player, block);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0,false, false));
    }

    public void reveal(Player player){
        if(!blocks.containsKey(player)) return;
        FallingBlock block = blocks.get(player);
        block.remove();
        blocks.remove(player);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

}
