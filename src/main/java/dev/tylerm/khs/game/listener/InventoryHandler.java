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

package dev.tylerm.khs.game.listener;

import com.cryptomorin.xseries.XMaterial;
import dev.tylerm.khs.Main;
import dev.tylerm.khs.command.map.Debug;
import dev.tylerm.khs.configuration.Map;
import dev.tylerm.khs.configuration.Maps;
import dev.tylerm.khs.game.util.Status;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        checkForInventoryMove(event);
        checkForSpectatorTeleportMenu(event);
        checkForDebugMenu(event);
        checkForBlockHuntMenu(event);
    }

    private void checkForInventoryMove(InventoryClickEvent event){
        if (Main.getInstance().getBoard().contains((Player) event.getWhoClicked()) && Main.getInstance().getGame().getStatus() == Status.STANDBY) {
            event.setCancelled(true);
        }
    }

    private void checkForSpectatorTeleportMenu(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
            
        ItemStack item = event.getCurrentItem();

        ItemMeta meta = item.getItemMeta();
        String name = meta.getDisplayName();
        
        if (Main.getInstance().getBoard().isSpectator(player)) {
            if (XMaterial.PLAYER_HEAD.isSimilar(item)) {
                event.setCancelled(true);
                player.closeInventory();
                Player clicked = Main.getInstance().getServer().getPlayer(name);
                if (clicked == null) return;
                player.teleport(clicked);
            } else if (XMaterial.ENCHANTED_BOOK.isSimilar(item)) {
                event.setCancelled(true);
                player.closeInventory();
                if (!name.startsWith("Page ")) return;
                String number_str = name.substring(5);
                try {
                    int page = Integer.parseInt(number_str);
                    InteractHandler.createSpectatorTeleportPage(player, page - 1);
                } catch(Exception ignored) {
                    return;
                }
            }
        }
    }

    private void checkForDebugMenu(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        boolean debug;
        if(Main.getInstance().supports(14)){
            debug = event.getView().getTitle().equals("Debug Menu") && player.hasPermission("hideandseek.debug");
        } else {
            debug = event.getInventory().getName().equals("Debug Menu") && player.hasPermission("hideandseek.debug");
        }
        if (debug){
            event.setCancelled(true);
            player.closeInventory();
            Debug.handleOption(player, event.getRawSlot());
        }
    }

    private void checkForBlockHuntMenu(InventoryClickEvent event){
        boolean test;
        String mapName;
        if(Main.getInstance().supports(14)){
            test = event.getView().getTitle().startsWith("Select a Block: ");
            if(!test) return;
            mapName = event.getView().getTitle().substring("Select a Block: ".length());
        } else {
            test = event.getInventory().getName().startsWith("Select a Block: ");
            if(!test) return;
            mapName = event.getInventory().getName().substring("Select a Block: ".length());
        }
        event.setCancelled(true);
        Map map = Maps.getMap(mapName);
        if(map == null) return;
        Material mat = map.getBlockHunt().get(event.getRawSlot());
        if(mat == null) return;
        Player player = (Player) event.getWhoClicked();
        Main.getInstance().getDisguiser().disguise(player, mat, map);
        player.closeInventory();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event){
        if (!(event.getPlayer() instanceof Player)) return;
        boolean test;
        String mapName;
        if(Main.getInstance().supports(14)){
            test = event.getView().getTitle().startsWith("Select a Block: ");
            if(!test) return;
            mapName = event.getView().getTitle().substring("Select a Block: ".length());
        } else {
            test = event.getInventory().getName().startsWith("Select a Block: ");
            if(!test) return;
            mapName = event.getInventory().getName().substring("Select a Block: ".length());
        }
        Map map = Maps.getMap(mapName);
        if(map == null) return;
        Material mat = map.getBlockHunt().get(0);
        if(mat == null) return;
        Player player = (Player) event.getPlayer();
        if(Main.getInstance().getDisguiser().disguised(player)) return;
        Main.getInstance().getDisguiser().disguise(player, mat, map);
        player.closeInventory();
    }

}
