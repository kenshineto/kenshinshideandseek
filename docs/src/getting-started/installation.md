# Installation

To install Kenshin's Hide and Seek (KHS), first get a working Minecraft server that is supported by the plugin. The following server softwares are suported.

## Spigot/Paper

Only the official JARs distributed by Spiggot or Paper are supported. Any forks of either of these server softwares are not expection to have perfect functionality.

Download the plugin from [Spigot](https://www.spigotmc.org/resources/kenshins-hide-and-seek.95306/) or [Modrinth](https://modrinth.com/plugin/kenshins-hide-and-seek) and place the JAR inside the servers `plugins/` folder.

Configuration files will be found in `plugins/KenshinsHideAndSeek`.

**Dependencies**

The only required dependency for bukkit server software is [PacketEvents](https://modrinth.com/plugin/packetevents).

**bStats**

KHS on bukkit server software uses bStats for metrics. You can disable bStats on the server to not have this information collected. See [bStats privacy policy](https://bstats.org/privacy-policy).

## Fabric

> [!WARNING]
> Fabric support is currently marked as experimental. Expect to find bugs or other issues.

Download the mod from [Modrinth](https://modrinth.com/plugin/kenshins-hide-and-seek) and place the JAR into the servers `mods/` folder.

Configuration files will be found in `config/khs`.

**Dependencies**

The following mods are required for KHS to function: [PacketEvents](https://modrinth.com/plugin/packetevents), [Fabric Langauge Kotlin](https://modrinth.com/mod/fabric-language-kotlin), [Fabric API](https://modrinth.com/mod/fabric-api/), and [Architectury](https://modrinth.com/mod/architectury-api).
