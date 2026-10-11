# Commands

This is a reference of all commands in the plugin.

Command arguments that start with an `*` are optional.

**Permissions**

All commands have auto generated permissions based on their command string. For example command `/hs map set border` has the permission `hs.map.set.border`.

## `/hs confirm`

Confirm an action that the plugin required confirmation for.

## `/hs debug`

Opens up a debug menu for the plugin.

## `/hs help`

**Arguments:** `*page`.

Messages the player a page of the help menu.

## `/hs join`

**Arguments:** `*map`.

Join the game lobby. If map is specified and the game lobby is empty, start the game lobby at that map.

## `/hs lastgame`

Show win/loose information from the last game played.

## `/hs leave`

Leave the game lobby.

## `/hs map add`

**Arguments:** `name`, `world`.

Create a map with a given name that will be located in a given world.

## `/hs map blockhunt block add`

**Arguments:** `map`, `block`.

Add a block to the pool that hiders can disguise themselves as in a blockhunt enabled map.

## `/hs map blockhunt block list`

**Arguments:** `map`.

List blocks that hiders can disguise themselves as in a given map.

## `/hs map blockhunt block remove`

**Arguments:** `map`, `block`.

Remove a block from the pool that hiders can disguise themselves as in a blockhunt enabled map.

## `/hs map blockhunt debug`

**Arguments:** `map`.

Open up the blockhunt picker for a given map.

## `/hs map blockhunt enabled`

**Arguments:** `map`, `enabled`.

Enable or disable blockhunt on a given map.

## `/hs map goto`

**Arguments:** `map`, `spawn`.

Goto the `lobby`, `seekerlobby`, or `spawn` of a given map.

> [!WARNING]
> If map save are enabled, `spawn` or `seekerlobby` will be located in the map save copy of the map, not the source map.

## `/hs map list`

List the maps available to the plugin and if each map is setup.

## `/hs map remove`

**Arguments:** `map`.

Remove the given map from the plugin.

## `/hs map save`

**Arguments:** `map`.

Initiate a map-save for a given map.

> [!WARNING]
> Commands cannot be run while a map save is in progress.

## `/hs set border`

**Arguments:** `map`, `size`, `delay`, `move`.

Set the maps world border to the command executors current position, with a given `size`. The border will shrink every `delay` seconds by `move` blocks.

## `/hs map set bounds`

**Arguments:** `map`.

Sets one of the two opposite corners of the maps bounds.

## `/hs map set lobby`

**Arguments:** `map`.

Sets the position of the maps lobby spawn.

## `/hs map set seekerlobby`

**Arguments:** `map`.

Sets the position of the maps seeker lobby spawn.

## `/hs map set spawn`

**Arguments:** `map`.

Sets the position of the maps game spawn.

## `/hs map status`

**Arguments:** `map`.

Run checks to see if a map is fully setup.

## `/hs map unset border`

**Arguments:** `map`.

Disable the border for a given map.

## `/hs reload`

Reload the plugins configuration.

## `/hs send`

**Arguments:** `map`.

Send the games lobby to a different map.

## `/hs setexit`

Sets the plugins global exit position.

## `/hs start`

**Arguments:** `*player`...

Manually starts the game. An optional list of players may be specified to be the initial seekers.

## `/hs stop`

Manually stop the game.

## `/hs top`

View the global win leaderboard for the plugin.

## `/hs wins`

**Arguments:** `*player`.

View the win and gameplay information for a given player. If the player argument is not set, the command instead shows the information for the player running the command.

## `/hs world create`

**Arguments:** `name`, `type`, `isFlat`.

Create a new miencraft world on the server with a given `type` and if the world is flat.

## `/hs world delete`

**Arguments:** `name`.

Delete a custom map from the minecraft server.

## `/hs world list`

List all available worlds to the minecraft server.

## `/hs world tp`

**Arguments:** `name`.

Teleport to the spawn point of a given world.
