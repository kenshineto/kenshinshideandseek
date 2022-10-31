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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Debug implements ICommand {

    private static final Map<Player, Map<Integer, Consumer<Player>>> debugMenuFunctions = new HashMap<>();

    public void execute(Player sender, String[] args) {
        net.tylermurphy.hideAndSeek.configuration.Map map = Maps.getMap(args[0]);
        if(map == null) {
            sender.sendMessage(errorPrefix + message("INVALID_MAP"));
            return;
        }
        Inventory debugMenu = createMenu(map, sender);
        sender.openInventory(debugMenu);
    }

    private Inventory createMenu(net.tylermurphy.hideAndSeek.configuration.Map map, Player sender){
        Map<Integer, Consumer<Player>> functions = new HashMap<>();
        Inventory debugMenu = Main.getInstance().getServer().createInventory(null, 18, "Debug Menu");
        debugMenu.setItem(0, createOption(functions, 0, XMaterial.LEATHER_CHESTPLATE.parseMaterial(), "&6Become a &lHider", 1, player -> {
            if(mapSaveEnabled) {
                if(map.getGameSpawn().getWorld() == null) map.getWorldLoader().loadMap();
            }
            Main.getInstance().getBoard().addHider(player);
            PlayerLoader.loadHider(player, map);
            if(Main.getInstance().getGame().getStatus() != Status.STARTING)
                PlayerLoader.resetPlayer(player, Main.getInstance().getBoard());
        }));
        debugMenu.setItem(1, createOption(functions, 1, XMaterial.GOLDEN_CHESTPLATE.parseMaterial(), "&cBecome a &lSeeker", 1, player -> {
            if(mapSaveEnabled) {
                if(map.getGameSpawn().getWorld() == null) map.getWorldLoader().loadMap();
            }
            Main.getInstance().getBoard().addSeeker(player);
            PlayerLoader.loadSeeker(player, map);
            if(Main.getInstance().getGame().getStatus() != Status.STARTING)
                PlayerLoader.resetPlayer(player, Main.getInstance().getBoard());
        }));
        debugMenu.setItem(2, createOption(functions, 2, XMaterial.IRON_CHESTPLATE.parseMaterial(), "&8Become a &lSpectator", 1, player -> {
            if(mapSaveEnabled) {
                if(map.getGameSpawn().getWorld() == null) map.getWorldLoader().loadMap();
            }
            Main.getInstance().getBoard().addSpectator(player);
            PlayerLoader.loadSpectator(player, map);
        }));
        debugMenu.setItem(3, createOption(functions, 3, XMaterial.BARRIER.parseMaterial(), "&cUnload from Game", 1, player -> {
            Main.getInstance().getBoard().remove(player);
            PlayerLoader.unloadPlayer(player);
            player.teleport(exitPosition);
        }));
        debugMenu.setItem(4, createOption(functions, 4, XMaterial.BARRIER.parseMaterial(), "&cDie In Game", 2, player -> {
            if((Main.getInstance().getBoard().isSeeker(player) || Main.getInstance().getBoard().isHider(player)) && Main.getInstance().getGame().getStatus() == Status.PLAYING){
                player.setHealth(0.1);
            }
        }));
        debugMenu.setItem(6, createOption(functions, 6, Material.ENDER_PEARL, "&d&lTeleport: &fGame spawn", 1, player -> {
            if(mapSaveEnabled) {
                if(map.getGameSpawn().getWorld() == null) map.getWorldLoader().loadMap();
            }
            player.teleport(map.getGameSpawn());
        }));
        debugMenu.setItem(7, createOption(functions, 7, Material.ENDER_PEARL, "&d&lTeleport: &fLobby", 2, player -> {
            player.teleport(map.getLobby());
        }));
        debugMenu.setItem(8, createOption(functions, 8, Material.ENDER_PEARL, "&d&lTeleport: &fExit", 3,  player -> player.teleport(exitPosition)));
        debugMenu.setItem(9, createOption(functions, 9, XMaterial.GLASS.parseMaterial(), "&dEnable Disguise", 1,  player -> {
            PlayerLoader.openBlockHuntPicker(player, map);
        }));
        debugMenu.setItem(10, createOption(functions, 10, XMaterial.PLAYER_HEAD.parseMaterial(), "&dDisable Disguise", 1, player -> Main.getInstance().getDisguiser().reveal(player)));
        debugMenuFunctions.put(sender, functions);
        return debugMenu;
    }

    private ItemStack createOption(Map<Integer, Consumer<Player>> functions, int slow, Material material, String name, int amount, Consumer<Player> callback){
        ItemStack temp = new ItemStack(material, amount);
        ItemMeta meta = temp.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        temp.setItemMeta(meta);
        functions.put(slow, callback);
        return temp;
    }

    public static void handleOption(Player player, int slotId){
        Main.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            Consumer<Player> callback = debugMenuFunctions.get(player).get(slotId);
            if(callback != null) callback.accept(player);
        }, 0);
    }

    public String getLabel() {
        return "debug";
    }

    public String getUsage() {
        return "<map>";
    }

    public String getDescription() {
        return "Run debug commands";
    }

    public List<String> autoComplete(String parameter) {
        if(parameter != null && parameter.equals("map")) {
            return Maps.getAllMaps().stream().map(net.tylermurphy.hideAndSeek.configuration.Map::getName).collect(Collectors.toList());
        }
        return null;
    }

}
