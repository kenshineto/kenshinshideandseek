package dev.tylerm.khs.configuration;

import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import dev.tylerm.khs.Main;
import dev.tylerm.khs.game.util.CountdownDisplay;
import dev.tylerm.khs.util.Location;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Config {

	private static ConfigManager config;

	public static String 
		messagePrefix,
		errorPrefix,
		tauntPrefix,
		worldBorderPrefix,
		abortPrefix,
		gameOverPrefix,
		warningPrefix,
		locale,
		leaveServer,
		placeholderError,
		placeholderNoData,
		databaseType,
		databaseHost,
		databasePort,
		databaseUser,
		databasePass,
		databaseName;
	
	public static boolean
		nameTagsVisible,
		permissionsRequired,
		announceMessagesToNonPlayers,
		tauntEnabled,
		tauntCountdown,
		tauntLast,
		alwaysGlow,
		glowEnabled,
		glowStackable,
		pvpEnabled,
		autoJoin,
		teleportToExit,
		lobbyCountdownEnabled,
		seekerPing,
		bungeeLeave,
		lobbyItemStartAdmin,
		leaveOnEnd,
		mapSaveEnabled,
		allowNaturalCauses,
		saveInventory,
		delayedRespawn,
        dontRewardQuit,
		spawnPatch,
		dropItems,
        respawnAsSpectator,
        waitTillNoneLeft,
        gameOverTitle,
		regenHealth;
	
	public static int 
		minPlayers,
		gameLength,
		tauntDelay,
		glowLength,
		countdown,
		changeCountdown,
		lobbyMin,
		lobbyMax,
		seekerPingLevel1,
		seekerPingLevel2,
		seekerPingLevel3,
		lobbyItemLeavePosition,
		lobbyItemStartPosition,
		flightToggleItemPosition,
		teleportItemPosition,
        startingSeekerCount,
		delayedRespawnDelay,
		hidingTimer,
        endGameDelay;

	public static float
		seekerPingLeadingVolume,
		seekerPingVolume,
		seekerPingPitch;

	public static List<String>
		blockedCommands,
		blockedInteracts;

	public static ItemStack
		lobbyLeaveItem,
		lobbyStartItem,
		glowPowerupItem,
		flightToggleItem,
		teleportItem;

	public static XSound
		ringingSound,
		heartbeatSound;

	public static CountdownDisplay
		countdownDisplay;

	public static Location
		exitPosition;
	
	public static void loadConfig() {

		config = ConfigManager.create("config.yml");
		config.saveConfig();

		announceMessagesToNonPlayers = config.getBoolean("announceMessagesToNonPlayers");

		//Prefix
		char SYMBOL = '§';
		String SYMBOL_STRING = String.valueOf(SYMBOL);

		messagePrefix = config.getString("prefix.default").replace("&", SYMBOL_STRING);
		errorPrefix = config.getString("prefix.error").replace("&", SYMBOL_STRING);
		tauntPrefix = config.getString("prefix.taunt").replace("&", SYMBOL_STRING);
		worldBorderPrefix = config.getString("prefix.border").replace("&", SYMBOL_STRING);
		abortPrefix = config.getString("prefix.abort").replace("&", SYMBOL_STRING);
		gameOverPrefix = config.getString("prefix.gameover").replace("&", SYMBOL_STRING);
		warningPrefix = config.getString("prefix.warning").replace("&", SYMBOL_STRING);

		// Locations
		exitPosition = new Location(
				config.getString("exit.world"),
				config.getInt("exit.x"),
				config.getInt("exit.y"),
				config.getInt("exit.z")
		);
		mapSaveEnabled = config.getBoolean("mapSaveEnabled");

		//Taunt
		tauntEnabled = config.getBoolean("taunt.enabled");
		tauntCountdown = config.getBoolean("taunt.showCountdown");
		tauntDelay = Math.max(60, config.getInt("taunt.delay"));
		tauntLast = config.getBoolean("taunt.whenLastPerson");

		//Glow
		alwaysGlow = config.getBoolean("alwaysGlow") && Main.getInstance().supports(9);
		glowLength = Math.max(1, config.getInt("glow.time"));
		glowStackable = config.getBoolean("glow.stackable");
		glowEnabled = config.getBoolean("glow.enabled") && Main.getInstance().supports(9) && !alwaysGlow;
		if (glowEnabled) {
			glowPowerupItem = createItemStack("glow");
		}

		//Lobby
        startingSeekerCount = Math.max(1, config.getInt("startingSeekerCount"));
        waitTillNoneLeft = config.getBoolean("waitTillNoneLeft");
		minPlayers = Math.max(1 + startingSeekerCount + (waitTillNoneLeft ? 0 : 1), config.getInt("minPlayers"));
		countdown = Math.max(10, config.getInt("lobby.countdown"));
		changeCountdown = Math.max(minPlayers, config.getInt("lobby.changeCountdown"));
		lobbyMin = Math.max(minPlayers, config.getInt("lobby.min"));
		lobbyMax = config.getInt("lobby.max");
		lobbyCountdownEnabled = config.getBoolean("lobby.enabled");

		//SeekerPing
		seekerPing = config.getBoolean("seekerPing.enabled");
		seekerPingLevel1 = config.getInt("seekerPing.distances.level1");
		seekerPingLevel2 = config.getInt("seekerPing.distances.level2");
		seekerPingLevel3 = config.getInt("seekerPing.distances.level3");
		seekerPingLeadingVolume = config.getFloat("seekerPing.sounds.leadingVolume");
		seekerPingVolume = config.getFloat("seekerPing.sounds.volume");
		seekerPingPitch = config.getFloat("seekerPing.sounds.pitch");
		Optional<XSound> heartbeatOptional = XSound.matchXSound(config.getString("seekerPing.sounds.heartbeatNoise"));
		heartbeatSound = heartbeatOptional.orElse(XSound.BLOCK_NOTE_BLOCK_BASEDRUM);
		Optional<XSound> ringingOptional = XSound.matchXSound(config.getString("seekerPing.sounds.ringingNoise"));
		ringingSound = ringingOptional.orElse(XSound.BLOCK_NOTE_BLOCK_PLING);

		//Other
		nameTagsVisible = config.getBoolean("nametagsVisible");
		permissionsRequired = config.getBoolean("permissionsRequired");
		gameLength = config.getInt("gameLength");
		pvpEnabled = config.getBoolean("pvp");
		allowNaturalCauses = config.getBoolean("allowNaturalCauses");
		autoJoin = config.getBoolean("autoJoin");
		teleportToExit = config.getBoolean("teleportToExit");
		locale = config.getString("locale", "local");
		blockedCommands = config.getStringList("blockedCommands");
		leaveOnEnd = config.getBoolean("leaveOnEnd");
		placeholderError = config.getString("placeholder.incorrect");
		placeholderNoData = config.getString("placeholder.noData");
		saveInventory = config.getBoolean("saveInventory");
        respawnAsSpectator = config.getBoolean("respawnAsSpectator");
        dontRewardQuit = config.getBoolean("dontRewardQuit");
        endGameDelay = Math.max(0,config.getInt("endGameDelay"));
        gameOverTitle = config.getBoolean("gameOverTitle");
		hidingTimer = Math.max(10, config.getInt("hidingTimer"));

		try {
			countdownDisplay = CountdownDisplay.valueOf(config.getString("hideCountdownDisplay"));
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("hideCountdownDisplay: "+config.getString("hideCountdownDisplay")+", is not a valid configuration option!");
		}
		blockedInteracts = new ArrayList<>();
		List<String> tempInteracts = config.getStringList("blockedInteracts");
		for(String id : tempInteracts) {
			Optional<XMaterial> optional_mat = XMaterial.matchXMaterial(id);
			if (optional_mat.isPresent()) {
				Material mat = optional_mat.get().parseMaterial();
				if (mat != null) {
					blockedInteracts.add(mat.name());
				}
			}
		}
		bungeeLeave = config.getString("leaveType") == null || config.getString("leaveType").equalsIgnoreCase("proxy");
		leaveServer = config.getString("leaveServer");

		//Lobby Items
		if (config.getBoolean("lobbyItems.leave.enabled")) {
			lobbyLeaveItem = createItemStack("lobbyItems.leave");
			lobbyItemLeavePosition = config.getInt("lobbyItems.leave.position");
		}
		if (config.getBoolean("lobbyItems.start.enabled")) {
			lobbyStartItem = createItemStack("lobbyItems.start");
			lobbyItemStartAdmin = config.getBoolean("lobbyItems.start.adminOnly");
			lobbyItemStartPosition = config.getInt("lobbyItems.start.position");
		}

		//Spectator Items
		flightToggleItem = createItemStack("spectatorItems.flight");
		flightToggleItemPosition = config.getInt("spectatorItems.flight.position");

		teleportItem = createItemStack("spectatorItems.teleport");
		teleportItemPosition = config.getInt("spectatorItems.teleport.position");

		//Database
		databaseHost = config.getString("databaseHost");
		databasePort = config.getString("databasePort");
		databaseUser = config.getString("databaseUser");
		databasePass = config.getString("databasePass");
		databaseName = config.getString("databaseName");

		databaseType = config.getString("databaseType").toUpperCase();
		if(!databaseType.equals("SQLITE") && !databaseType.equals("MYSQL")){
			throw new RuntimeException("databaseType: "+databaseType+" is not a valid configuration option!");
		}

		delayedRespawn = config.getBoolean("delayedRespawn.enabled");
		delayedRespawnDelay = Math.max(0,config.getInt("delayedRespawn.delay"));

		spawnPatch = config.getBoolean("spawnPatch");
		dropItems = config.getBoolean("dropItems");
		regenHealth = config.getBoolean("regenHealth");

	}
	
	public static void addToConfig(String path, Object value) {
		config.set(path, value);
	}

	public static void saveConfig() {
		config.saveConfig();
	}

	@Nullable
	private static ItemStack createItemStack(String path){
		ConfigurationSection item = new YamlConfiguration().createSection("temp");
		item.set("name", ChatColor.translateAlternateColorCodes('&',config.getString(path+".name")));
		item.set("material", config.getString(path+".material"));
		if (Main.getInstance().supports(14)) {
			if (config.contains(path+".model-data") && config.getInt(path+".model-data") != 0) {
				item.set("model-data", config.getInt(path+".model-data"));
			}
		}
		List<String> lore = config.getStringList(path+".lore");
		if (lore != null && !lore.isEmpty()) item.set("lore", lore);
		ItemStack temp = null;
		try{ temp = XItemStack.deserialize(item); } catch(Exception ignored) {}
		return temp;
	}
	
}
