# Border

> [!WARNING]
> The border event uses the maps global world border. There should not be anything else going on in the world the game is in with the border event enabled or it will affect players not in the game.

The border is shrinking field of play that gets smaller and smaller as the game progresses.

The border is enabled on a per-map basis. To enable to border on a map run. `/hs map set border <map> <size> <delay> <move>`. To disable the border run `/hs map unset border <map>`.

The position of the world border will be set to the position of the player that run `/hs map set border`.

**Values**

These are the values that are passed in to the `/hs map set border` command.

> [!NOTE]
> The delay in seconds does not include the 30 seconds the border takes to shrink each interval.

| name | description | type |
| ---- | ----------- | ---- |
| map | The name of the map to enable the border on. | string |
| size | The size that the world border starts at in block radius. | uint |
| delay | The length on seconds the border spends between shrinking. | uint |
| move | The amount of blocks the border shrinks each interval. | uint |
