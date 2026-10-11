# Board

## Lobby

Part of sub-section `lobby`.

The following values can be used within the template. Values can be used within the template by typing `{NAME}` where NAME is the name of the template value.

| name | description |
| ---- | ----------- |
| COUNTDOWN | Time left until game start, or a message that there is not enough players. |
| COUNT | Number of players currently in the lobby. |
| SEEKER% | Percentage change that the player will be a seeker next game. |
| HIDER% | Percentage change that the player will be a hider next game. |
| MAP | The current map the lobby is in. |

Change what is displayed on the scoreboard/leaderboard while in the lobby.

| name | description | type |
| ---- | ----------- | ---- |
| title | The title of the scoreboard. | string |
| content | The template to generate the scoreboard from. | string |

## Game

Part of sub-section `game`.

The following values can be used within the template. Values can be used within the template by typing `{NAME}` where NAME is the name of the template value. If a template value is `toggable`, any lines with that value will be removed from the scoreboard if the action/event associated with that value is disabled.

| name | description | toggable |
| ---- | ----------- | -------- |
| TIME | Time left in seconds until the game ends. | false |
| TEAM | The team the player is currently on. | false |
| BORDER | The current status of the world border event. | true |
| TAUNT | The current status of the taunt event. | true |
| GLOW | The current status of the glow power-up. | true |
| #SEEKER | The current number of seekers in the game. | false |
| #HIDER | The current number of hiders in the game. | false |
| MAP | The current map the lobby is in. | false |

Chant what is displayed on the scoreboard/leaderboard while playing the game.

| name | description | type |
| ---- | ----------- | ---- |
| title | The title of the scoreboard. | string |
| content | The template to generate the scoreboard from. | string |

## Countdown

Part of sub-section `countdown`.

| name | description | type |
| ---- | ----------- | ---- |
| waiting | Text displayed when the lobby is waiting for more players. | string |
| startingIn | Text displayed when the lobby is starting in some seconds. | string |
| timer | How many minutes and seconds until game end. | string |

## Taunt

Part of sub-section `taunt`.

| name | description | type |
| ---- | ----------- | ---- |
| timer | How many minutes and seconds until the next taunt. | string |
| active | Text shown when a player is about to be taunted. | string |

## Glow

Part of sub-section `glow`.

| name | description | type |
| ---- | ----------- | ---- |
| active | Text shown when the glow poer-up is active. | string |
| disabled | Text shown when the glow poer-up is inactive. | string |

## Border

Part of sub-section `border`.

| name | description | type |
| ---- | ----------- | ---- |
| timer | How many minutes and seconds until the world border shrinks. | string |
| shrinking | Text shown when the world border is shrinking. | string |
