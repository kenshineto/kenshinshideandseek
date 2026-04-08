![](https://github.com/kenshineto/kenshinshideandseek2/blob/main/img/banner.png?raw=true)

**A Minecraft Blockhunt and Hide and Seek Plugin**
- Fully configurable
- Customizable scoreboards
- Database to store wins/losses/kills/deaths
- Block hunt with custom disguises
- Multiple maps per server

**Supported Versions: 1.8.x - 26.1.x**

**Installation**
1. Make sure to be running a Spigot/Paper server
2. Install Packet Events to your server
3. Download the latest version of this plugin to your server
4. Restart the server, and it should be up and running!
5. Read the [wiki](https://github.com/kenshineto/kenshinshideandseek2/blob/main/.wiki/2-Setup.md) for in game setup instructions

**Reporting Bugs**
Instead of leaving a review with the error message, please report issues [here](https://github.com/kenshineto/kenshinshideandseek2/issues).

**Contributing**
Please send PRs [here](https://github.com/kenshineto/kenshinshideandseek2/pulls) and make sure to read the CONTRIBUTING.md file in the repository.

**Migrating from 1.x**

This plugin was recently rewritten for a 2.0.0 release.

The database will be migrated automatically, so please don't delete it. As always, backup the database before updating.

All configs from the 1.x series are incompatible, and will have to be reconfigured.

**Features**
- This plugin supports both Blockhunt and regular Hide and Seek game-modes, which can be toggled in the config.yml
- A Glow power-up for Hiders to view where nearby Seekers are located. Can be disabled and configured.
- A border that slowly closes in as the game progresses, killing those who are left on the outside. Can be disabled and configured.
- A taunt system that randomly selects hiders and lets seekers know of their presence. Can be disabled and configured.
- Player's wins, losses, kills, deaths, and hider and seeker statics are stored in a SQLite or MySQL/MariaDB database.
- Fully lobby system where players can join, automatic starting, and custom scoreboards.
- Fully custom in game scoreboard that can display teams, power-up information, event information, and game information.
- Total message localization that allows players to change any message in the plugin to whatever they want. English and German localizations have been premade, but still can be altered if wished.
- Custom items and effects can be given out to either the Hiders and Spectators for when they are in the game.
- A complete spectator mode with player teleporting.

**Config Files**
1. **config.yml** - The general configuration file which contains most of the plugins configuration options along with help text to help server owners understand the options.
2. **items.yml** - Sets what items and effects each team gets while in the game
3. **board.yml** - Sets what is displayed on the lobby or in game scoreboard
4. **locale.yml** - Sets what text is displayed by the plugin. Can be used to change the wording of the message, or even the language
