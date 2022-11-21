/*
 * This file is part of Kenshins Hide and Seek
 *
 * Copyright (c) 2021 Tyler Murphy.
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

package net.tylermurphy.hideAndSeek;

import net.tylermurphy.hideAndSeek.command.*;
import net.tylermurphy.hideAndSeek.command.map.*;
import net.tylermurphy.hideAndSeek.command.map.blockhunt.Enabled;
import net.tylermurphy.hideAndSeek.command.map.set.*;
import net.tylermurphy.hideAndSeek.command.world.Create;
import net.tylermurphy.hideAndSeek.command.world.Delete;
import net.tylermurphy.hideAndSeek.command.world.Tp;
import net.tylermurphy.hideAndSeek.configuration.*;
import net.tylermurphy.hideAndSeek.database.Database;
import net.tylermurphy.hideAndSeek.game.*;
import net.tylermurphy.hideAndSeek.command.util.CommandGroup;
import net.tylermurphy.hideAndSeek.game.listener.*;
import net.tylermurphy.hideAndSeek.util.PAPIExpansion;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class Main extends JavaPlugin implements Listener {
	
	private static Main instance;
	private static int version;

	private Database database;
	private Board board;
	private Disguiser disguiser;
	private EntityHider entityHider;
	private Game game;
	private CommandGroup commandGroup;
	private boolean loaded;

	public void onEnable() {

		long start = System.currentTimeMillis();

		getLogger().info("Loading Kenshin's Hide and Seek");
		Main.instance = this;

		getLogger().info("Getting minecraft version...");
		this.updateVersion();;

		try {
			getLogger().info("Loading config.yml...");
			Config.loadConfig();
			getLogger().info("Loading maps.yml...");
			Maps.loadMaps();
			getLogger().info("Loading localization.yml...");
			Localization.loadLocalization();
			getLogger().info("Loading items.yml...");
			Items.loadItems();
			getLogger().info("Loading leaderboard.yml...");
			Leaderboard.loadLeaderboard();
		} catch (Exception e) {
			getLogger().severe(e.getMessage());
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		getLogger().info("Creating internal scoreboard...");
		this.board = new Board();
		getLogger().info("Connecting to database...");
		this.database = new Database();
		getLogger().info("Loading disguises...");
		this.disguiser = new Disguiser();
		getLogger().info("Loading entity hider...");
		this.entityHider = new EntityHider(this, EntityHider.Policy.BLACKLIST);
		getLogger().info("Registering listeners...");
		this.registerListeners();

		getLogger().info("Registering commands...");
		this.commandGroup = new CommandGroup("hs",
				new Help(),
				new Reload(),
				new Join(),
				new Leave(),
				new Send(),
				new Start(),
				new Stop(),
				new CommandGroup("map",
						new CommandGroup("blockhunt",
								new CommandGroup("blocks",
									new net.tylermurphy.hideAndSeek.command.map.blockhunt.blocks.Add(),
									new net.tylermurphy.hideAndSeek.command.map.blockhunt.blocks.Remove(),
									new net.tylermurphy.hideAndSeek.command.map.blockhunt.blocks.List()
								),
								new Enabled()
						),
						new CommandGroup("set",
								new Lobby(),
								new Spawn(),
								new SeekerLobby(),
								new Border(),
								new Bounds()
						),
						new Add(),
						new Remove(),
						new List(),
						new Status(),
						new Save(),
						new Debug(),
						new GoTo()
				),
				new CommandGroup("world",
					new Create(),
					new Delete(),
					new net.tylermurphy.hideAndSeek.command.world.List(),
					new Tp()
				),
				new SetExitLocation(),
				new Top(),
				new Wins(),
				new Confirm()
		);

		getLogger().info("Loading game...");
		game = new Game(null, board);

		getLogger().info("Scheduling tick tasks...");
		getServer().getScheduler().runTaskTimer(this, this::onTick,0,1).getTaskId();

		getLogger().info("Registering outgoing bungeecord plugin channel...");
		Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		getLogger().info("Checking for PlaceholderAPI...");
		if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
			getLogger().info("PlaceholderAPI found...");
			getLogger().info("Registering PlaceholderAPI expansion...");
			new PAPIExpansion().register();
		}

		long end = System.currentTimeMillis();
		getLogger().info("Finished loading plugin ("+(end-start)+"ms)");
		loaded = true;

	}

	public void onDisable() {

		version = 0;

		if(board != null) {
			board.getPlayers().forEach(player -> {
				board.removeBoard(player);
				PlayerLoader.unloadPlayer(player);
				exitPosition.teleport(player);
			});
			board.cleanup();
		}

		if(disguiser != null) {
			disguiser.cleanUp();
		}

		Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
	}

	private void onTick() {
		if(game.getStatus() == net.tylermurphy.hideAndSeek.game.util.Status.ENDED) game = new Game(game.getCurrentMap(), board);
		game.onTick();
		disguiser.check();
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new BlockedCommandHandler(), this);
		getServer().getPluginManager().registerEvents(new ChatHandler(), this);
		getServer().getPluginManager().registerEvents(new DamageHandler(), this);
		getServer().getPluginManager().registerEvents(new DisguiseHandler(), this);
		getServer().getPluginManager().registerEvents(new InteractHandler(), this);
		getServer().getPluginManager().registerEvents(new InventoryHandler(), this);
		getServer().getPluginManager().registerEvents(new JoinLeaveHandler(), this);
		getServer().getPluginManager().registerEvents(new MovementHandler(), this);
		getServer().getPluginManager().registerEvents(new PlayerHandler(), this);
		getServer().getPluginManager().registerEvents(new RespawnHandler(), this);
	}

	private void updateVersion(){
		Matcher matcher = Pattern.compile("MC: \\d\\.(\\d+)").matcher(Bukkit.getVersion());
		if (matcher.find()) {
			version = Integer.parseInt(matcher.group(1));
		} else {
			throw new IllegalArgumentException("Failed to parse server version from: " + Bukkit.getVersion());
		}
	}
	
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(errorPrefix + message("COMMAND_PLAYER_ONLY"));
			return true;
		}
		commandGroup.handleCommand((Player)sender, args);
		return true;
	}
	
	public java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(errorPrefix + message("COMMAND_PLAYER_ONLY"));
			return new ArrayList<>();
		}
		return commandGroup.handleTabComplete((Player)sender, args);
	}

	public static Main getInstance() {
		return instance;
	}

	public File getWorldContainer() {
		return this.getServer().getWorldContainer();
	}

	public Database getDatabase() {
		return database;
	}

	public Board getBoard(){
		return board;
	}

	public Game getGame(){
		return game;
	}

	public Disguiser getDisguiser() { return disguiser; }

	public EntityHider getEntityHider() { return entityHider; }

	public CommandGroup getCommandGroup() { return commandGroup; }

	public boolean supports(int v){
		return version >= v;
	}

	public java.util.List<String> getWorlds() {
		java.util.List<String> worlds = new ArrayList<>();
		File[] containers = getWorldContainer().listFiles();
		if(containers != null) {
			Arrays.stream(containers).forEach(file -> {
				if (!file.isDirectory()) return;
				String[] files = file.list();
				if (files == null) return;
				if (!Arrays.asList(files).contains("session.lock") && !Arrays.asList(files).contains("level.dat")) return;
				worlds.add(file.getName());
			});
		}
		return worlds;
	}

	public boolean isLoaded() {
		return loaded;
	}
	
}