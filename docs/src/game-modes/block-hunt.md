# Block hunt

> [!NOTE]
> Block hunt is not supported on Minecraft 1.8.

Block hunt is not necessarily a game mode itself, but an extension of the other game modes. When enabled on a map, hiders will be prompted to disguise as a block. If hit or moved, the block will become undisguised and will be visible to other hiders or seekers. If stationey for a period of time, the block will solidify and will blend in with the environment. The rest of the game runs as it normally would.

Spiggot/PaperMC servers use falling sand as the undisguised block while Fabric uses display entities. This doesn't matter for gameplay, but it's to be noted.

> [!NOTE]
> Players disguised can see their own player character but other players will not be able to.

**Setup**

Block hunt is enabled on a per-map basis, and is disabled by default. To enable or disable block hunt on a map, run `/hs map blockhunt enabled <map> <true/false>`.

For a map to pass all checks, it must have at least one block in its blockhunt selection. To add or remove blocks run, `/hs map blockhunt block <add/remove> <map> <block>`. To list a map's blocks run `/hs map blockhunt block list <map>`.
