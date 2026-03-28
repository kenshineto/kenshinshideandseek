### About
Get information about the plugin
```
Usage: /hs about
Permission: hs.about
```

### Map Add
Adds a map to the plugin
```
Usage: /hs map add <name>
Permission hs.map.add
```

### Map Remove
Removes a map from the plugin
```
Usage: /hs map remove <name>
Permission hs.map.remove
```

### Map List
Lists all maps
```
Usage: /hs map list
Permission hs.map.list
```

### Map GoTo
Goes to a spawn point on a map
```
Usage: /hs map goto <name> <point>
Permission hs.map.goto
```

### Map Save
Creates a map save for a map
```
Usage: /hs map save <name>
Permission hs.map.save
```

### Map Status
Tells what checks fail for a given map
```
Usage: /hs map status <map>
Permission hs.map.status
```

### Map Set Spawn
Sets the global spawn for a map
```
Usage: /hs map set spawn <name>
Permission hs.map.set.spawn
```

### Map Set Lobby
Sets the lobby point for a map
```
Usage: /hs map set lobby <name>
Permission hs.map.set.lobby
```

### Map Set Seeker Lobby
Sets the seeker lobby/waiting area for a map
```
Usage: /hs map set seekerlobby <name>
Permission hs.map.set.seekerlobby
```

### Map Set Bounds
Sets one of the bounds coordinates at a given position for a map
```
Usage: /hs map set bounds <name>
Permission hs.map.set.bounds
```

### Map Set Border
Set the world border data for a map
```
Usage: /hs map set border <name> <*size> <*delay> <*move>
Permission hs.map.set.border
```

### Map Blockhunt Enabled
Sets if blockhunt is enabled on a given map
```
Usage: /hs map blockhunt enabled <name> <true/false>
Permission hs.map.blockhunt.enabled
```

### Map Blockhunt Debug
Opens up the blockhunt picker for a map. Use `hs /debug` to remove disguises.
```
Usage: /hs map blockhunt debug <name>
Permission hs.map.blockhunt.debug
```

### Map Blockhunt Blocks Add
Adds a block to a maps blockhunt block choice
```
Usage: /hs map blockhunt block add <name> <block>
Permission hs.map.blockhunt.block.add
```
### Map Blockhunt Blocks Remove
Remove a block from a maps blockhunt block choice
```
Usage: /hs map blockhunt block remove <name> <block>
Permission hs.map.blockhunt.block.remove
```
### Map Blockhunt Blocks List
List a maps blockhunt block
```
Usage: /hs map blockhunt block list <name>
Permission hs.map.blockhunt.block.list
```

### World Create
Create a new world on the server
```
Usage: /hs world create <name> <type>
Permission hs.world.create
```

### World Delete
Delete a world on the server
```
Usage: /hs world delete <name>
Permission hs.world.delete
```

### World List
List all worlds on the server
```
Usage: /hs world list
Permission hs.world.list
```

### World TP
Teleport to the other world
```
Usage: /hs world tp <name>
Permission hs.world.tp
```

### Confirm
Confirm a prompt from another command
```
Usage: /hs confirm
Permission hs.confirm
```

### Debug
Debug the current game, or remove a block hunt disguise.
```
Usage: /hs debug
Permission hs.debug
```

### Help
Get the commands for the plugin
```
Usage: /hs help
Permission: hs.help
```

### Join
Joins the lobby if game is set to manual join/leave
```
Usage: /hs join
Permission: hs.join
```

### Leave
Leaves the lobby if game is set to manual join/leave
```
Usage: /hs leave
Permission: hs.leave
```

### Reload
Reloads the config files
```
Usage: /hs reload
Permission: hs.reload
```

### Send
Send the game lobby to another map
```
Usage: /hs send <name>
Permission hs.send
```

### Set Exit
Sets the hide and seek exit location to current position and world.
```
Usage: /hs setExit
Permission: hs.setexit
```

### Start
Starts the hide and seek game for those in the lobby. You can also manually select a seeker.
```
Usage: /hs start <seeker>
Permission: hs.start
```

### Stop
Force stop the hide and seek game, and return players to lobby.
```
Usage: /hs stop
Permission: hs.stop
```

### Top
Gets the top players in the server
```
Usage: /hs top <page>
Permission: hs.top
```

### Wins
Get the win information for yourself or another player
```
Usage: /hs wins <player>
Permission: hs.wins
```
