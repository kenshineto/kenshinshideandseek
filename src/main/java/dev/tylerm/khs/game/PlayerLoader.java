/*
 * This file is part of Kenshins Hide and Seek
 *
 * Copyright (c) 2022 Tyler Murphy.
 *
 * Kenshins Hide and Seek free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * he Free Software Foundation version 3.
 *
 * Kenshins Hide and Seek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package dev.tylerm.khs.game;

import com.cryptomorin.xseries.messages.Titles;
import net.md_5.bungee.api.ChatColor;
import dev.tylerm.khs.Main;
import dev.tylerm.khs.configuration.Items;
import dev.tylerm.khs.configuration.Map;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static dev.tylerm.khs.configuration.Config.*;
import static dev.tylerm.khs.configuration.Items.HIDER_ITEMS;
import static dev.tylerm.khs.configuration.Items.SEEKER_ITEMS;
import static dev.tylerm.khs.configuration.Localization.message;

@SuppressWarnings("deprecation")
public class PlayerLoader {

    public static void loadHider(Player player, Map map){
        map.getGameSpawn().teleport(player);
        loadPlayer(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,1000000,5,false,false));
        Titles.sendTitle(player, 10, 70, 20, ChatColor.WHITE + "" + message("HIDER_TEAM_NAME"), ChatColor.WHITE + message("HIDERS_SUBTITLE").toString());
        if(map.isBlockHuntEnabled()){
            openBlockHuntPicker(player, map);
        }
    }

    public static void loadSeeker(Player player, Map map){
        map.getGameSeekerLobby().teleport(player);
        loadPlayer(player);
        Titles.sendTitle(player, 10, 70, 20, ChatColor.WHITE + "" + message("SEEKER_TEAM_NAME"), ChatColor.WHITE + message("SEEKERS_SUBTITLE").toString());
    }

    public static void loadSpectator(Player player, Map map){
        map.getGameSpawn().teleport(player);
        loadPlayer(player);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFallDistance(0.0F);
        player.getInventory().setItem(flightToggleItemPosition, flightToggleItem);
        player.getInventory().setItem(teleportItemPosition, teleportItem);
        Main.getInstance().getBoard().getPlayers().forEach(otherPlayer -> otherPlayer.hidePlayer(player));
        Titles.sendTitle(player, 10, 70, 20, ChatColor.GRAY + "" + ChatColor.BOLD + "SPECTATING", ChatColor.WHITE + message("SPECTATOR_SUBTITLE").toString());
    }

    public static void loadDeadHiderSpectator(Player player, Map map) {
        map.getGameSpawn().teleport(player);
        loadPlayer(player);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFallDistance(0.0F);
        player.getInventory().setItem(flightToggleItemPosition, flightToggleItem);
        player.getInventory().setItem(teleportItemPosition, teleportItem);
        Main.getInstance().getBoard().getPlayers().forEach(otherPlayer -> otherPlayer.hidePlayer(player));
    }

    public static void resetPlayer(Player player, Board board){
        if(board.isSpectator(player)) return;
        loadPlayer(player);
        if (board.isSeeker(player)) {
            if (pvpEnabled) {
                for(int i = 0; i < 9; i++) {
                    if (SEEKER_ITEMS.get(i) == null) continue;
                    player.getInventory().setItem(i, SEEKER_ITEMS.get(i));
                }
                if (Items.SEEKER_HELM != null)
                    player.getInventory().setHelmet(Items.SEEKER_HELM);
                if (Items.SEEKER_CHEST != null)
                    player.getInventory().setChestplate(Items.SEEKER_CHEST);
                if (Items.SEEKER_LEGS != null)
                    player.getInventory().setLeggings(Items.SEEKER_LEGS);
                if (Items.SEEKER_BOOTS != null)
                    player.getInventory().setBoots(Items.SEEKER_BOOTS);
            }
            for(PotionEffect effect : Items.SEEKER_EFFECTS)
                player.addPotionEffect(effect);
        } else if (board.isHider(player)) {
            if (pvpEnabled) {
                for(int i = 0; i < 9; i++) {
                    if (HIDER_ITEMS.get(i) == null) continue;
                    player.getInventory().setItem(i, HIDER_ITEMS.get(i));
                }
                if (Items.HIDER_HELM != null)
                    player.getInventory().setHelmet(Items.HIDER_HELM);
                if (Items.HIDER_CHEST != null)
                    player.getInventory().setChestplate(Items.HIDER_CHEST);
                if (Items.HIDER_LEGS != null)
                    player.getInventory().setLeggings(Items.HIDER_LEGS);
                if (Items.HIDER_BOOTS != null)
                    player.getInventory().setBoots(Items.HIDER_BOOTS);
            }
            for(PotionEffect effect : Items.HIDER_EFFECTS)
                player.addPotionEffect(effect);
            if (glowEnabled) {
                player.getInventory().addItem(glowPowerupItem);
            }
        }
    }

    public static void unloadPlayer(Player player){
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        Main.getInstance().getDisguiser().reveal(player);
        for(PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        if (Main.getInstance().supports(9)) {
            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attribute != null) player.setHealth(attribute.getValue());
            for(Player temp : Main.getInstance().getBoard().getPlayers()) {
                Main.getInstance().getGame().getGlow().setGlow(player, temp, false);
            }
        } else {
            player.setHealth(player.getMaxHealth());
        }
        Main.getInstance().getBoard().getPlayers().forEach(temp -> {
            player.showPlayer(temp);
            temp.showPlayer(player);
        });
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setFallDistance(0.0F);
    }

    public static void joinPlayer(Player player, Map map){
        map.getLobby().teleport(player);
        loadPlayer(player);
        if (lobbyStartItem != null && (!lobbyItemStartAdmin || player.hasPermission("hideandseek.start")))
            player.getInventory().setItem(lobbyItemStartPosition, lobbyStartItem);
        if (lobbyLeaveItem != null)
            player.getInventory().setItem(lobbyItemLeavePosition, lobbyLeaveItem);
    }

    private static void loadPlayer(Player player){
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        for(PotionEffect effect : player.getActivePotionEffects()) {
            if(effect.getType().getName().equals("INVISIBILITY") && Main.getInstance().getDisguiser().disguised(player)) continue;
            player.removePotionEffect(effect.getType());
        }
        player.setFoodLevel(20);
        if (Main.getInstance().supports(9)) {
            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attribute != null) player.setHealth(attribute.getValue());
        } else {
            player.setHealth(player.getMaxHealth());
        }
    }

    public static void openBlockHuntPicker(Player player, Map map){
        int slots = ((map.getBlockHunt().size()-1)/9)*9+9;
        Inventory inventory = Main.getInstance().getServer().createInventory(null, slots, "Select a Block: " + map.getName());
        for(int i=0;i<map.getBlockHunt().size();i++){
            inventory.setItem(i, new ItemStack(map.getBlockHunt().get(i)));
        }
        player.openInventory(inventory);
    }

}
