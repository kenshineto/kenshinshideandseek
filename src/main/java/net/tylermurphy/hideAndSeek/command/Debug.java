package net.tylermurphy.hideAndSeek.command;

import com.cryptomorin.xseries.XMaterial;
import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.configuration.Maps;
import net.tylermurphy.hideAndSeek.game.PlayerLoader;
import net.tylermurphy.hideAndSeek.game.util.Status;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Debug implements ICommand {

    private static final Map<Integer, Consumer<Player>> debugMenuFunctions = new HashMap<>();
    private Inventory debugMenu;

    public void execute(Player sender, String[] args) {
        if(args.length < 1) args = new String[]{""};
        if(debugMenu == null) createMenu(args[0]);
        sender.openInventory(debugMenu);
    }

    private void createMenu(String mapname){
        net.tylermurphy.hideAndSeek.configuration.Map map = Maps.getMap(mapname);
        debugMenu = Main.getInstance().getServer().createInventory(null, 18, "Debug Menu");
        debugMenu.setItem(0, createOption(0, XMaterial.LEATHER_CHESTPLATE.parseMaterial(), "&6Become a &lHider", 1, player -> {
            if(mapSaveEnabled) {
                if(Main.getInstance().getGame().getCurrentMap().getSpawn().getWorld() == null) Main.getInstance().getGame().getCurrentMap().getWorldLoader().loadMap();
            }
            Main.getInstance().getBoard().addHider(player);
            PlayerLoader.loadHider(player, Main.getInstance().getGame().getCurrentMap());
            if(Main.getInstance().getGame().getStatus() != Status.STARTING)
                PlayerLoader.resetPlayer(player, Main.getInstance().getBoard());
        }));
        debugMenu.setItem(1, createOption(1, XMaterial.GOLDEN_CHESTPLATE.parseMaterial(), "&cBecome a &lSeeker", 1, player -> {
            if(mapSaveEnabled) {
                if(Main.getInstance().getGame().getCurrentMap().getSpawn().getWorld() == null) Main.getInstance().getGame().getCurrentMap().getWorldLoader().loadMap();
            }
            Main.getInstance().getBoard().addSeeker(player);
            PlayerLoader.loadSeeker(player, Main.getInstance().getGame().getCurrentMap());
            if(Main.getInstance().getGame().getStatus() != Status.STARTING)
                PlayerLoader.resetPlayer(player, Main.getInstance().getBoard());
        }));
        debugMenu.setItem(2, createOption(2, XMaterial.IRON_CHESTPLATE.parseMaterial(), "&8Become a &lSpectator", 1, player -> {
            if(mapSaveEnabled) {
                if(Main.getInstance().getGame().getCurrentMap().getSpawn().getWorld() == null) Main.getInstance().getGame().getCurrentMap().getWorldLoader().loadMap();
            }
            Main.getInstance().getBoard().addSpectator(player);
            PlayerLoader.loadSpectator(player, Main.getInstance().getGame().getCurrentMap());
        }));
        debugMenu.setItem(3, createOption(3, XMaterial.BARRIER.parseMaterial(), "&cUnload from Game", 1, player -> {
            Main.getInstance().getBoard().remove(player);
            PlayerLoader.unloadPlayer(player);
            player.teleport(exitPosition);
        }));
        debugMenu.setItem(4, createOption(4, XMaterial.BARRIER.parseMaterial(), "&cDie In Game", 2, player -> {
            if((Main.getInstance().getBoard().isSeeker(player) || Main.getInstance().getBoard().isHider(player)) && Main.getInstance().getGame().getStatus() == Status.PLAYING){
                player.setHealth(0.1);
            }
        }));
        debugMenu.setItem(6, createOption(6, Material.ENDER_PEARL, "&d&lTeleport: &fGame spawn", 1, player -> {
            if(map == null) {
                player.sendMessage(errorPrefix + message("INVALID_MAP"));
                return;
            }
            if(mapSaveEnabled) {
                if(map.getSpawn().getWorld() == null) map.getWorldLoader().loadMap();
            }
            player.teleport(map.getSpawn());
        }));
        debugMenu.setItem(7, createOption(7, Material.ENDER_PEARL, "&d&lTeleport: &fLobby", 2, player -> {
            if(map == null) {
                player.sendMessage(errorPrefix + message("INVALID_MAP"));
                return;
            }
            player.teleport(map.getLobby());
        }));
        debugMenu.setItem(8, createOption(8, Material.ENDER_PEARL, "&d&lTeleport: &fExit", 3,  player -> player.teleport(exitPosition)));
        debugMenu.setItem(9, createOption(9, XMaterial.GLASS.parseMaterial(), "&dEnable Disguise", 1,  player -> {
            if(map == null) {
                player.sendMessage(errorPrefix + message("INVALID_MAP"));
                return;
            }
            PlayerLoader.openBlockHuntPicker(player, map);
        }));
        debugMenu.setItem(10, createOption(10, XMaterial.PLAYER_HEAD.parseMaterial(), "&dDisable Disguise", 1, player -> Main.getInstance().getDisguiser().reveal(player)));
    }

    private ItemStack createOption(int slow, Material material, String name, int amount, Consumer<Player> callback){
        ItemStack temp = new ItemStack(material, amount);
        ItemMeta meta = temp.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        temp.setItemMeta(meta);
        debugMenuFunctions.put(slow, callback);
        return temp;
    }

    public static void handleOption(Player player, int slotId){
        Main.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            Consumer<Player> callback = debugMenuFunctions.get(slotId);
            if(callback != null) callback.accept(player);
        }, 0);
    }

    public String getLabel() {
        return "debug";
    }

    public String getUsage() {
        return "<*map>";
    }

    public String getDescription() {
        return "Run debug commands";
    }

}
