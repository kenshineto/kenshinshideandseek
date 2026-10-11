# Setup

Before players are able to even join the plugin's game lobby, a map must first be created and setup.

**Creating a map**

A map is a space in which the game is played in. To create a map run `/hs map add <name> <world>`.

**Spawn points**

Each map needs its own lobby, seeker spawn, and hider spawn. To set these run the following commands at the location of the spawn point.
- `/hs map set lobby <name>` - is where players wait for the round to start for that map
- `/hs map set seekerlobby <name>` - is where seekers wait to join or during death cooldown till they spawn/respawn into the game
- `/hs map set spawn <name>` - is where everyone spawns into the map.

Because the seeker lobby is where seekers wait, it's important to make it a closed off space and not have a view to the map itself. Otherwise, seekers could start attacking early, or watch where the hiders go hide.

**Bounds**

Map bounds are a square bounding box that must contain the map, global spawn, and seeker lobby. This is used to contain all players and spectators in the game. To set the bounds run `/hs map set bounds <name>` in two opposite corners of the map. You will see that the bounds is set to (1/2), then (2/2). If the bounds are set to the wrong positions, running the command again will repeat the (1/2), and (2/2) steps.

**Map save**

The plugin saves a copy of the map to play on so that original doesn't get modified. Otherwise, a player can open the doors, mess with redstone, or other actions that would stay persistant.

Map saves may be disabled with the `mapsvae` option in the config if rollback functionality is not wanted. If disabled, this section should be ignored.

To start a map save, make sure spawn points and bounds are already set, then run `/hs map save <name>` and the map will be saved.

> [!WARNING]
> Commands cannot be run while a map save is in progress.

> [!WARNING]
> The map save must be re-run every time changes are made to the source map.

**Finally**

To check if a map passes all checks run `/hs map status <map>`, and it will state if all checks pass or what checks failed. Only maps that pass all checks will be added into the map pool.

If this is the first map made, a global exit must be set before any gameplay can begin. This is where all players go when they leave the game. This can be set by running `/hs setexit` at the wished exit position.

Run `/hs join` to join the lobby. `/hs start` to start the game. `/hs leave` to leave the game!
