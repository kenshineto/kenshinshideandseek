package dev.tylerm.khs.game;

import static dev.tylerm.khs.configuration.Config.*;
import static dev.tylerm.khs.configuration.Localization.message;

import dev.tylerm.khs.game.util.Disguise;
import dev.tylerm.khs.configuration.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class Disguiser {

    private final HashMap<Player, Disguise> disguises;

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
    public Disguise getByHitBoxID(int ID){
        return disguises.values().stream().filter(disguise -> disguise.getHitBoxID() == ID).findFirst().orElse(null);
    }

    public void check(){
        for(HashMap.Entry<Player, Disguise> set : disguises.entrySet()){
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

    public void disguise(Player player, Material material, Map map){
        if(!map.isBlockHuntEnabled()){
            player.sendMessage(errorPrefix + message("BLOCKHUNT_DISABLED"));
            return;
        }
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

    public void cleanUp() {
        disguises.values().forEach(Disguise::remove);
    }

}
