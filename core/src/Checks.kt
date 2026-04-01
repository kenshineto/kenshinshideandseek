package cat.freya.khs

import cat.freya.khs.game.Game
import cat.freya.khs.game.KhsMap
import cat.freya.khs.player.Player
import cat.freya.khs.world.Position

class Checks(val plugin: Khs, val player: Player) {
    /// checks if there exists a map that is set up
    fun gameMapExists() {
        if (plugin.game.selectMap() == null) {
            val msg =
                if (plugin.maps.isEmpty()) plugin.locale.map.none else plugin.locale.map.noneSetup
            error(msg)
        }
    }

    /// checks that the game is in progress
    fun gameInProgress() {
        if (!plugin.game.status.inProgress()) {
            error(plugin.locale.game.notInProgress)
        }
    }

    /// checks that the game is not in progress
    fun gameNotInProgress() {
        if (plugin.game.status != Game.Status.LOBBY) {
            error(plugin.locale.game.inProgress)
        }
    }

    /// checks that the caller is in the game
    fun playerNotInGame() {
        if (plugin.game.hasPlayer(player)) {
            error(plugin.locale.game.inGame)
        }
    }

    /// checks that the caller is in the game
    fun playerInGame() {
        if (!plugin.game.hasPlayer(player)) {
            error(plugin.locale.game.notInGame)
        }
    }

    /// check if the lobby has enough players to start
    fun lobbyHasEnoughPlayers() {
        if (plugin.game.size < plugin.config.minPlayers) {
            error(plugin.locale.lobby.notEnoughPlayers.with(plugin.config.minPlayers))
        }
    }

    /// check if the lobby is empty
    fun lobbyEmpty() {
        if (plugin.game.size > 0u) {
            error(plugin.locale.lobby.inUse)
        }
    }

    /// cheks that the player is in the game world
    fun inMapWorld(mapName: String) {
        inMapWorld(plugin.maps[mapName])
    }

    /// cheks that the player is in the game world
    fun inMapWorld(map: KhsMap?) {
        if (map?.worldName != player.location.worldName) error(plugin.locale.map.wrongWorld)
    }

    /// Checks that the map exists and is set up
    fun mapSetup(map: KhsMap?) {
        if (map == null) error(plugin.locale.map.unknown)
        if (!map.setup) error(plugin.locale.map.setup.not.with(map.name))
    }

    /// Checks if a map exists
    fun mapExists(name: String) {
        mapExists(plugin.maps[name])
    }

    /// Checks if a map exists
    fun mapExists(map: KhsMap?) {
        if (map == null) error(plugin.locale.map.unknown)
    }

    /// Checks if a map doesnt exists
    fun mapDoesNotExist(name: String) {
        if (plugin.maps.containsKey(name)) error(plugin.locale.map.exists)
    }

    /// Checks if a map name is valid
    fun mapNameValid(name: String) {
        if (!name.matches(Regex("[a-zA-Z0-9]*")) || name.isEmpty())
            error(plugin.locale.map.invalidName)
    }

    /// Checks if a world exists
    fun worldExists(worldName: String) {
        if (!plugin.shim.worlds.contains(worldName))
            error(plugin.locale.world.doesntExist.with(worldName))
    }

    /// Checks if a world doesnt exists
    fun worldDoesNotExist(worldName: String) {
        if (plugin.shim.worlds.contains(worldName))
            error(plugin.locale.world.exists.with(worldName))
    }

    /// Checks if a world is valid for a map
    fun worldValid(worldName: String) {
        worldExists(worldName)
        if (worldName.startsWith("hs_")) error(plugin.locale.world.doesntExist.with(worldName))
    }

    /// Checks that a world is not in use
    fun worldNotInUse(worldName: String) {
        val map =
            plugin.maps.values.find { it.worldName == worldName || it.gameWorldName == worldName }
        if (map != null) error(plugin.locale.world.inUseBy.with(worldName, map.name))
        if (plugin.config.exit?.worldName == worldName)
            error(plugin.locale.world.inUse.with(worldName))
    }

    /// Checks if blockhunt is supported
    fun blockHuntSupported() {
        if (!plugin.shim.supports(9)) error(plugin.locale.blockHunt.notSupported)
    }

    /// Checks if a map has block hunt enabled
    fun blockHuntEnabled(name: String) {
        mapExists(name)
        val map = plugin.maps[name] ?: return
        if (!map.config.blockHunt.enabled) error(plugin.locale.blockHunt.notEnabled)
    }

    private fun isSpawnInRange(map: KhsMap, position: Position?): Boolean {
        if (position == null) return true // return true to not reset a null value

        // check world border (with in 100 blocks)
        val border = map.config.worldBorder
        if (border.enabled && (border.pos?.distance(position) ?: 0.0) > 100.0) return false

        // check in bounds
        if (map.bounds()?.inBounds(position.x, position.z) == false) return false

        return true
    }

    /// Makes sure a spawn is in game
    fun spawnInRange(map: KhsMap, pos: Position) {
        if (!isSpawnInRange(map, pos)) error(plugin.locale.map.error.notInRange)
    }

    /// Makes sure spawns are in range
    fun spawnsInRange(map: KhsMap) {
        // check game spawn
        if (!isSpawnInRange(map, map.gameSpawn?.position)) {
            player.message(plugin.locale.prefix.warning + plugin.locale.map.warn.gameSpawnReset)
            map.gameSpawn = null
        }
        // check seeker spawn
        if (!isSpawnInRange(map, map.seekerLobbySpawn?.position)) {
            player.message(plugin.locale.prefix.warning + plugin.locale.map.warn.seekerSpawnReset)
            map.seekerLobbySpawn = null
        }
        // check lobby spawn
        if (!isSpawnInRange(map, map.lobbySpawn?.position)) {
            player.message(plugin.locale.prefix.warning + plugin.locale.map.warn.lobbySpawnReset)
            map.lobbySpawn = null
        }
    }
}

fun runChecks(plugin: Khs, player: Player, fn: Checks.() -> Unit) {
    fn(Checks(plugin, player))
}
