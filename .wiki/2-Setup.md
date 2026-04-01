## Standard Setup
This plugin functions by having players join a lobby. Then a game will start and the players will be transferred to the game map. A random player will be selected as a seeker. If pvp is enabled, players are given items specified in the items.yml, and are required to kill other players to "find" them. If the time runs out, the hiders win. If a hider is found, they become a seeker. If everyone becomes a seeker, the seekers win. Players that join mid game will become spectators. Once the game ends, players will be teleported back to the lobby. Players then can leave the lobby and will be teleported to the exit position.

### **Download Requirements**
The Packet Events plugin is required for the plugin to work. You can download it at their [spigotmc](https://www.spigotmc.org/resources/packetevents-api.80279/) page or their [modrinth](https://modrinth.com/plugin/packetevents) page.

### **Setting up a map**
A map is a space in which the game is played in. To create a map run `/hs map add <name> <world>`.

#### Spawn points
Now each map needs its own lobby, seeker spawn, and hider spawn. To set these run...

`/hs map set lobby <name>`, `/hs map set seekerlobby <name>`, and `/hs map set spawn <name>` at the location of the spawn point.

- **Lobby** is where players wait for the round to start for that map
- **Seeker Lobby** is where seekers wait to join or during death cooldown till they spawn/respawn into the game
- **Hider/game spawn** is where everyone spawns into the map.

Because the seeker lobby is where seekers wait, it's important to make it a closed off space and not have a view to the map itself. Otherwise, seekers could start attacking early, or watch where the hiders go to hide.

#### Bounds
Next, you have to set up the map's bounds, which is the square bounding box that must contain the map, global spawn, and seeker lobby. This is used to contain all players and spectators in the game. To set the bounds run, `/hs map set bounds <name>` in two opposite corners of the map. You will see that the bounds is set to (1/2), then (2/2). If you set the bounds to the wrong positions, then just run it two more times, and it will repeat the (1/2), and (2/2) steps. If you accidentally run it an extra time and it goes to (1/2), then just run it one more time anywhere to get it to (2/2) to set your bounds again.

#### Map save
The plugin saves a copy of the map to play on so that original doesn't get modified. Otherwise, a player can change the doors, trapdoors, or anything else, and it will stay that way each round.

If you still don't want to use map saves, you can disable the `mapsave` option in the config, and skip this part.

To set the map save, make sure you have done everything else before this, spawn points and bounds. Just run `/hs map save <name>` and the map will be saved.

**Warning**, commands cannot be run while a map save is in progress.

#### World Border
The world border is optional. But if you want to set one, just run `/hs map set border <name> <size> <delay> <move>`. The size is the `size` of the world border. The `delay` is the interval in which the border shrinks. The `move` is how much the border shrinks each interval.

To delete the world border on a map just run `/hs map set border <name>` without any of the other arguments.

#### Blockhunt (Minecraft 1.9+)
Block hunt is enabled on a per-map basis, and is disabled by default. To enable or disable block hunt on a map, just run `/hs map blockhunt enabled <map> <true/false>`. For a map to pass all checks, it must have at least one block in its blockhunt selection. To add or remove blocks just run, `/hs map blockhunt block <add/remove> <map> <block>`. To list a map's blocks run `/hs map blockhunt block list <map>`.

#### Finally
To check if a map passes all checks run, `/hs map status <map>`, and it will tell you if all checks pass, or what checks failed. Only maps that pass all checks will be added into the map pool.

If this is the first map made, you also need to set the global exit. This is where all players go when they leave the game. You can set this by running '/hs setexit` at the position you wish to set it at. If you would rather have players teleported to another server on a bungeecord or velocity setup, just change the exit type and destination in the config.yml.

**You are now done with the basic setup!** 🥳 Run `/hs join` to join the lobby. `/hs start` to start the game. `/hs leave` to leave the game!
