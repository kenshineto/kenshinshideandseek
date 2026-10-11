# Config

 This section is for the `config.yml` file.

The `config.yml` file contains all the general settings for the plugin, ranging from player count to active game mode. Each option is listed below along with its default value, type, and description for that setting. Information for all of these settings will also be stated in the `config.yml` file.

## General

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| checkForUpdates | Check for updates on server startup, and notify players with the `hs.admin` permission. | bool | true |
| dropItems | Allow players to drop their items mid-game. | bool | false |
| countdownDisplay | Where the plugin will state the length of time in seconds left to hide. Allowed values are CHAT, ACTIONBAR, or TITLE. | enum | CHAT |
| nametagsVisible | Allow hiders' to see everyone's nametags. Seekers' can never see nametags. | bool | false |
| permmissionsRequired | Require players to have permissions to run commands. | bool | true |
| startingSeekerCount | Amount of initial seekers when teh game starts, minimum of 1. | uint | 1 |
| respawnAsSpectator | Respawn dead hiders as spectators instead of seekers. | bool | false |
| gameOverTitle | Display a title describing the game over along with the chat message. | bool | true |
| blockHuntNotify | Notify a seeker when the undisguise a hider in block hunt. | bool | true |
| debug | Show plugin debug output in the server console. | bool | false |

**Spectator items**

Part of sub-section `spectatorItems`.

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| flight | The item a spectator can use to toggle flight. | item | |
| teleport | The item a spectator can use to teleport to other hiders or seekers. | item | |

**Seeker ping**

Part of sub-section `seekerPing`.

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| enabled | Play a ping sound when seekers' get too close to a hider. | bool | true |
| distances.level1 | Seekers this distance from the hider cause a low-intensity ping noise. | uint | 30 |
| distances.level1 | Seekers this distance from the hider cause a medium-intensity ping noise. | uint | 20 |
| distances.level1 | Seekers this distance from the hider cause a high-intensity ping noise. | uint | 10 |
| sounds.heartbeatNoise | The heartbeat noise that speeds up the closer seekers get. | sound | basedrum |
| sounds.ringingNoise | The ringing noise that plays when a seeker is very close (level3). | sound | pling |
| sounds.leadingVolume | The volume used for the first note of the ping noise. | decimal | 0.5 |
| sounds.volume | The volume used for the rest of the ping noise. | decimal | 0.3 |
| sounds.pitch | The pitch of the ping noise. | decimal | 1.0 |

## Timing

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| gameLength | How long in seconds wil the game last, set to 0 to make the game length infinite. | ulong | 1200 |
| hidingLength | How long in seconds will the initial hiding period last, minimum is 10 seconds. | ulong | 30 |
| endGameDelay | How long in seconds the game will wait until the it teleports players to the lobby after a game over is triggered. | ulong | 5 |

**Delayed respawn**

Part of sub-section `delayedRespawn`.

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| enabled | Seekers will have to wait [delay] seconds until they respawn after death. | bool | true |
| delay | How long in secodns dp player shave to wait in seconds before respawning. | uint | 5 |

## Database

The database is used to save persistant game information such as player names, and how many wins/losses a player has.

There are three supported database types: `SQLITE`, `MYSQL`, and `POSTGRES`. When using `SQLITE` which is the default, all other arguments besides `type` should be ignored as SQLITE is not a remote databse. Remote database arguments only matter for `MYSQL` or `POSTGRES`.

It is recommended to stick with `SQLITE` for most setups as no work is needed to be done by the server admins. But given a multi-server setup, a remote database is recommended to have shared data between the servers.

**Database**

Part of sub-section `database`.

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| type | The type of database to store user data in. | enum | SQLITE |
| host | The hostname of the remote database server. | string | localhost |
| port | The port of the remote database server. | optional ulong | null |
| username | The username to connect with to the remote database server. | string | postgres |
| password | The password to connect with to the remote database server. | string | postgres |
| database | The name of the database to connect to on the remote database server. | string | postgres |

## Game mode

There currently are two supported game modes: `HIDE_AND_SEEK` and `TAG`.

When playng `HIDE_AND_SEEK`, the two available scoring modes are: `ALL_HIDERS_FOUND`, or `LAST_HIDER_WINS`.

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| gameMode | The game mode that the plugin will operate under. | enum | HIDE_AND_SEEK |
| scoringMode | The scoring mode decides the hider win condition for the HIDE_AND_SEEK game moed only. | enum | ALL_HIDERS_FOUND |
| dontRewardQuit | End the game abruptly without updating any scores if a player leaving the game causes a game over. | bool | true |

## PvP

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| pvp | Seekers must kill hiders to "find" them. Hiders are able to attack seekers to protect themselves. | bool | true |
| regenHealth | Allow players to regenate health. | bool | false |
| allowNaturalCauses | Hiders and Seekers can no longer take damage from natural causes such as fall damage or projectiles. | bool | false |

## Lobby

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| autoJoin | Players will auto join the game lobby upon joining the server. | bool | false |
| teleportStraysToExit | Players will be teleported to the plugin's exit position if they join the server and are located in a game world. | bool | false |
| leaveType | `EXIT` will teleport players to the plugin's exit position upon leaving the game. `PROXY` will instead teleport them to the `leaveOnEnd` server. | enum | EXIT |
| leaveServer | The bungeecord/velocity server players will teleport to upon a leave when leaveType is set to `PROXY`. | string | "lobby" |
| leaveOnEnd | All players will always leave the game upon a game over. | bool | false |
| saveInventory | Restore the players previously cleared inventory after leaving the game. | bool | false |
| saveScoreBoard | Restore the players previously active score board after leaving the game. | bool | true |

**Lobby**

Part of sub-section `lobby`.

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| countdown | Time in seconds the lobby waits until the game starts. Set to 0 to disable. | ulong | 60 |
| chagneCountdown | Player threshold to speed up the countdown to 10 seconds. Set to 0 to disable. |
| min | Minimum amount of players required to start the countdown. | uint | 3 |
| max | Maximum amount of players allowed in the lobby. | uint | 10 |
| leaveItem | Item used by players to leave the lobby. | item | |
| startItem | Item used by admins to manually start the game. | item | |

## Events

**Taunt**

Part of sub-section `taunt`.

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| enabled | If the taunt event is enabled. | bool | true |
| delay | Time in esconds between taunts, minimum is 60 seconds. | ulong | 360 |
| disableForLastHider | Don't taunt when there is only one hider left. | bool | false |
| showCountdown | Allow seekers to see the time till next taunt, not just hiders. | bool | true |

## Power-ups

**Glow**

Part of sub-section `glow`.

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| enabled | If the glow power-up is to be given to hiders. | bool | true |
| time | The length in seconds that the power-up lasts. | ulong | 30 |
| stackable | Allows multiple uses of the power-up to stack the duration instead of resetting the timer. | bool | true |
| item | The item hiders use to activate the power-up. | item | |

## Protections

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| mapSaveEnabled | Duplicate copes of maps to their own world to protect the original from changes during the game. The plugin will rollback the map save after gameplay. | bool | true |
| blockedCommands | Commands players are not allowed to run during gameplay. | list | |
| blockedInteracts | Blocks players are not allowed to interact with during gameplay. | list | |

## Command hooks

Trigger custom commands after game events.

**Command hooks**

Part of sub-section `commandHooks`.

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| enable | The plugin will eecute the hook commands for each player. | bool | false |
| onGameStart | List of commands to execute for each player upon starting the game. | list | [] |
| onGameEndWin | List of commands to execute for each winning player. | list | [] |
| onGameEndLoose | List of commands to execute for each loosing player. | list | [] |
