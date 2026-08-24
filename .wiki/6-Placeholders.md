### **Placeholders**

This plugin supports software such as [PlaceholderAPI](https://placeholderapi.com/)

A placeholder is a string such as `%hs_team%`, which in this case returns what team you are on

#### Teams

The following teams: `hiders`, `seekers`, and `spectators` can be used in `%hs_<team>%` to get
the number of players currently in that team.

Also `%hs_team%` returns your current team.

#### Rankings

The valid player statistics are:
- `wins`, `hiderWins`, `seekerWins`
- `losses`, `hiderLosses`, `seekerLosses`
- `games`, `hiderGames`, `seekerGames`
- `kills`, `hiderKills`, `seekerKills`
- `deaths`, `hiderDeaths`, `seekerDeaths`

**To get the RANK of a statistic for a target use:**

`%hs_rank_<stat>_<target>%`

- When target is not set, it returns the subject players integer rank
- When target is a player name/uuid, it returns the targets integer rank
- When target is an integer rank, it returns the player name at that rank

**Examples:**
- %hs_rank_wins%
- %hs_rank_deaths_KenshinEto$
- %hs_rank_games_1%

#### Stats

**To get the VALUE of a statistic for a target use:**

`%hs_stat_<stat>_<target>%`

- When target is not set, it returns the subject players stat value
- When target is a player name/uuid, it returns the stat value for that player
- When target is an integer rank, it returns the stat value at that ranking place (1st/2nd/3rd/...)

**Examples:**
- %hs_stat_wins%
- %hs_stat_deaths_KenshinEto$
- %hs_stat_games_1%

#### Last Winners

The following placeholders return information about the last game played.

`%hs_win_<team>_<index>%` - Returns list of winners
`%hs_loose_<team>_<index>%` - Returns liust of loosers

- When team is set, it will only return players in that list that are in the given team
- When index is setm it will return the player at the list at the given index. The index starts at zero.

**Examples:**
- %hs_last_win%
- %hs_last_win_seeker%
- %hs_last_win_hider%
- %hs_last_win_hider_0%
- %hs_last_loose%
- %hs_last_loose_hider_2%
