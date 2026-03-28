## Config
**This section is for the config.yml file**

The config.yml file contains all of the general settings for the plugin, ranging from region local, to player count. Each option is listed below along with its default value, type, and description for that setting. Information for all of these settings will also be stated in the config.yml file.

| Name | Default | Type | Description |
| ------------- | ------------- |------------- | ------------- |
| dropItems | false | bool | Allows players to drop items from their inventory. |
| countdownDisplay | CHAT | enum | Where countdown messages are displayed. |
| nametagsVisible | false | bool | Allow Hiders to see their own teams nametags as well as seekers. Seekers can never see nametags |
| permissionsRequired | true | bool | Require bukkit permissions though a plugin such as LuckPerms to run commands. Only disable if you trust ALL players |
| minPlayers | 2 | int | Minimum amount of players to start the game |
| startingSeekerCount | 1 | int | Amount of initial seekers when the game starts |
| respawnAsSpectator | false | bool | When a hider does, respawn as a spectator instead of a seeker |
| gameOverTitle | false | bool | Along with a chat message, display a title describing the game over |
| spectatorItems.flight | | item | The item a spectator is given to toggle flight |
| spectatorItems.teleport | | item | The item a spectatir is given to teleprot to other players |
| s
| seekerPing.enabled | true | bool | If the seeker ping is enabled |
| seekerPing.distances | 30, 20, 10 | int | The 3 distances in blocks where the different levels of seeker ping will play |
| seekerPing.sounds.heartbeatNoise | BLOCK_NOTE_BLOCK_BASEDRUM | [Sound](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html) | | The sound of the heartbeast in the seeker ping |
| seekerPing.sounds.ringingNoise | BLOCK_NOTE_BLOCK_PLING | [Sound](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html) | | The sound of the ringing in the seeker ping |
| seekerPing.sounds.leadingVolume | 0.5 | float | The first notes volume in the seeker ping cycle |
| seekerPing.sounds.volume | 0.3 | float | The rest of the notes volumes in the seeker ping cycle |
| seekerPing.sounds.pitch | 1 | float | The pitch of the seeker ping notes |
| gameLength  | 1200 | int | How long in seconds will the game last, set it < 1 to disable |
| hidingLength | 30 | int | How long do hiders have to hide before seekers are released |
| endGameDelay | 5 | int | How long after the game ends till the next map is loaded |
| delayedRespawn.enabled | true | bool | If enabled, seekers must wait a cooldown until respawning back into the map besides instally being respawned. |
| database.type | SQLITE | enum | The type of database the plugin will try to connect to: SQLITE, MYSQL, POSTGRES |
| database.host | | String | Only needed if not using sqlite. The host for the database. |
| database.port | | String | Only needed if not using sqlite. The port for the database. |
| database.username | | String | Only needed if not using sqlite. The username for the database. |
| database.password | | String | Only needed if not using sqlite. The password for the database. |
| database.database | | String | Only needed if not using sqlite. The database name for the database. |
| scoringMode | ALL_HIDERS_FOUND | enum | By default the game ends when all hiders have been found. Use LAST_HIDER_WINDS for the last hider wins |
| dontRewardQuit | true | bool | If the last hider quits, dont reward a seeker win |
| pvp | true | bool | This plugin by default functions as not tag to catch Hiders, but to pvp. All players are given weapons, and seekers slightly better weapons (this can be changed in items.yml). If you want, you can disable this entire pvp functionality, and make Hiders get found on a single hit. Hiders would also not be able to fight back against Seekers if disabled.
| allowNaturalCauses | false | bool | If pvp is disabled, allow players to get hurt by falling, drowning, or other natural causes |
| autoJoin | false | bool | Players that join the server will automatically be placed into the lobby |
| teleportStraysToExit | false | bool | (When autoJoin is false), when players join the world containing the lobby, they are automatically teleported to the designated exit position so that they possibly don't spawn in the lobby while not in the queue. Anyone who ever joins in the game world (the duplicated world where the game is played) will always be teleported out regardless. |
| leaveType | EXIT | enum | When running /hs leave, should they leave the lobby, or be sent back to a bungeecord hub server? |
| leaveServer | hub | String | If leaveType is set to PROXY, this is the name of the server to send people to as the bungeecord hub server |
| leaveOnEnd | false | bool | Empty the game lobby at the end of each game |
| lobby.countdown | 60 | int | The lobby default countdown wait time if a certain player treashold hasnt been met |
| lobby.changeCountdown | 5 | int | Sets the lobby countdown timer to 10 seconds if this may players join the lobby |
| lobby.min | 3 | int | Minimum number of players to auto start the game |
| lobby.max | 10 | int | Maximum number of players that can join the lobby |
| lobby.leaveItem | bed | item | The item lobby members can interact with to leave the lobby |
| lobby.startItem | clock | item | The item admins can interact with to force start the game |
| saveInventory | false | bool | If enabled a playesr inventory will be saved upon entering a lobby and given back when they leave the lobby |
| taunt.enabled | true | bool | if the taunt event is enabled |
| taunt.delay | 360 | int | how often in seconds a random taunt will trigger |
| taunt.disableForLastHider | false | bool | allow the taunt to occur on the last player |
| taunt.showCountdown | true | bool | show when the next taunt will occur |
| glow.enabled | true | bool | if the glow powerup is enabled for hiders |
| glow.time | 30 | int | how long in seconds the glow powerup will go for |
| glow.stackable | true | bool | if two players use it at the same time, the timers will stack |
| glow.item | snowball | item | the item hiders can interact with to enable the glow powerup |
| alwaysGlow | false | bool | Always shows all seekers positions to each hider by giving them a glow effect. Disiables the glow powerup if enabled. Only on 1.9+ |
| mapSaveEnabled | true | bool | If set to false, the mapSave will be ignored and it will just use the original map. Warning: if set to false, there will be no rolling back of any changed to the map during gameplay. |
| blockedCommands | \[msg, tp, gamemode, kill, give, effect] | String Array | An array/list of commands that will be blocked from running for any user in the game |
| blockedInteracts | \[FURNACE, CRAFTING_TABLE, ANVIL, CHEST, BARREL] | [Material](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html) Array | An array/list of Materials that when interactd with will do nothing if the user is in the lobby or game |

## Localization
**This section is for the locale.yml file**

This file is simple, it allows you to change the text of messages that the plugin ueses.

## Leaderboard
**This section is for the board.yml file**

This config file allows you to change what is displayed on the scoreboard\leaderboard while in the lobby, or in the game. Below are a list of predefined placeholders.

#### LOBBY BOARD PLACEHOLDERS

| Name | Description |
| ------------- | ------------- |
| {COUNTDOWN} | Displays the time left in the lobby countdown. If there are not enough people in the lobby, or the lobby countdown its disabled, it just displays waiting for players. The text displayed can be changed below. |
| {COUNT} | The amount of player currently in the lobby. |
| {SEEKER%} | The chance that a player will be selected to be a seeker. |
| {HIDER%} | The chance that a player will be selected to be a hider. |
| {MAP} | The current map that the game is on. |

#### GAME BOARD PLACEHOLDERS

| Name | Description |
| ------------- | ------------- |
| {TIME} | The amount of time left in the game in MmSs. |
| {TEAM} | The team you are on. Hider, Seeker, or Spectator. |
| {BORDER} | The current status of the world boarder, if enabled. If the world border is disabled, this line is removed automatically. Displays the time left until the border moves in MmSs, or "Decreasing" if it's decreasing. What is displayed exactly can be changed below. |
| {TAUNT} | The current status of the taunt system, if enabled. If taunts are disabled, any line with {TAUNT} will be automatically removed. Shows the time left till next taunt in MmSs, if the taunt is active, and if the taunt has expired (one player left). What is displayed exactly can be changed below. |
| {GLOW} | The current status of the glow powerup, if enabled. This line is automatically removed if the glow poewrup is disabled. Tells all players if a Glow powerup is active, only Hiders will be able to see its effects though. |
| {#SEEKER} | Number of current seekers. |
| {#HIDER} | Number of current hiders. |
| {MAP} | The current map the game is on. |

You can use theese placeholders to specify the content of the leaderboards on the side of the screen in both the lobby and while in game. You can also set the speific types of text different place holders will return as well in the config file.

## Items
**This section is for the items.yml file**

If you have pvp enabled, this file will specify what items each player will get. There are two sections for items, one for hiders, and one for seekers. Spectators don't get any items. Below is a table on the parameters, and some examples on some premade items.

| Name | Type | Description |
| ------------- | ------------- | ------------- |
| material | [Material](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html) | What item to give |
| enchantments | [EnchantingID](https://minecraft.fandom.com/el/wiki/Enchanting/ID) : int| The name of the enchantment, followed by a `:`, then the enchantment level. |
| name | String | Items name |
| unbreakable | bool | If the item is unbreakable |
| lore | String list | List of lore strings |

**Weapon Example**
```
- material: DIAMOND_SWORD
  enchantments:
    sharpness: 1
  name: 'Seeker Sword'
  unbreakable: true
  lore:
    - 'This is the seeker sword'
```

You can also give potions to your players. Below is a table on what you need, and some examples on how to do it.

| Name | Type | Description |
| ------------- | ------------- | ------------- |
| material | [Material](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html) | POTION or SPLASH_POTION |

**Potion Example**
```
- material: POTION:INSTANT_HEAL
  name: 'this is a potion!'
```

**Splash Potion Example**
```
- material: SPLASH_POTION:REGEN
  lore: [heals you]
```

The next section of the file is effects. These are the effects players on each team will receive after the initial hiding countdown finishes. Again, this section is broken into effects seekers get, and effects hiders get.

You will need to specify the following information:

| Name | Type | Description |
| ------------- | ------------- | ------------- |
| type | [PotionEffectType](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html) | The type of potion effect. |
| duration | int | The duration in seconds for the effect to last. Put 1000000 for it to last until the the player switches teams or the game ends |
| amplifier | int | The minecraft effects amplifier. |
| ambient | bool | Makes potion effect produce more, translucent, particles. |
| particles | bool | If the effect should produce particles at all. |

**Effect Example**
```
- type: WATER_BREATHING
  duration: 1000000
  amplifier: 1
  ambient: false
  particles: false
```
