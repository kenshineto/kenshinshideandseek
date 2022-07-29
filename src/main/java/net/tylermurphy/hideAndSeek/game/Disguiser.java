package net.tylermurphy.hideAndSeek.game;

import net.tylermurphy.hideAndSeek.game.util.Disguise;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

public class Disguiser {

    private final Map<Player, Disguise> disguises;

    public Disguiser(){
        this.disguises = new HashMap<>();
    }

    public Disguise getDisguise(Player player){
        return disguises.get(player);
    }

    public boolean disguised(Player player) { return disguises.containsKey(player); }

    @Nullable
    public Disguise getByEntityID(int ID){
        return disguises.values().stream().filter(disguise -> disguise.getEntityID() == ID).findFirst().orElse(null);
    }

    @Nullable
    public Disguise getByBlockLocation(BlockVector loc){
        return disguises.values().stream().filter(disguise -> {
            if(disguise.getSolidLocation() == null) return false;
            return disguise.getSolidLocation().toVector().toBlockVector() == loc;
        }).findFirst().orElse(null);
    }

    public void check(){
        for(Map.Entry<Player, Disguise> set : disguises.entrySet()){
            Disguise disguise = set.getValue();
            Player player = set.getKey();
            if(!player.isOnline()) {
                disguise.remove();
                disguises.remove(player);
            } else {
                disguise.update();
            }
        }
    }

    public void disguise(Player player, Material material){
        if(disguises.containsKey(player)){
            disguises.get(player).remove();
        }
        Disguise disguise = new Disguise(player, material);
        disguises.put(player, disguise);
    }

    public void reveal(Player player){
        if(disguises.containsKey(player))
            disguises.get(player).remove();
        disguises.remove(player);
    }

}
