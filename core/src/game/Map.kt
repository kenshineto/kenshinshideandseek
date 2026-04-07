package cat.freya.khs.game

import cat.freya.khs.Khs
import cat.freya.khs.config.MapConfig
import cat.freya.khs.world.Location
import cat.freya.khs.world.Position
import cat.freya.khs.world.World

class KhsMap(val name: String, var config: MapConfig, var plugin: Khs) {

    /** The world where the map is created and where the lobby is hosted */
    lateinit var worldName: String
        private set

    /**
     * The name of the world where the game takes place. If map saves are enabled, the name is
     * prefixed with hs_
     */
    lateinit var gameWorldName: String
        private set

    var gameSpawn: Location? = null
    var lobbySpawn: Location? = null
    var seekerLobbySpawn: Location? = null

    init {
        reloadConfig()
    }

    fun reloadConfig() {
        worldName = config.world ?: error("map '$name' has no world set!")
        gameWorldName = if (plugin.config.mapSaveEnabled) "hs_$worldName" else worldName
        gameSpawn = config.spawns.game?.toPosition()?.toLocation(gameWorldName)
        lobbySpawn = config.spawns.lobby?.toLocation(worldName)
        seekerLobbySpawn = config.spawns.seeker?.toLocation(gameWorldName)
    }

    fun getWorld(): World? {
        return plugin.shim.getWorld(worldName)
    }

    fun getGameWorld(): World? {
        return plugin.shim.getWorld(gameWorldName)
    }

    fun getWorldLoader(): World.Loader {
        return plugin.shim.getWorldLoader(worldName)
    }

    fun getGameWorldLoader(): World.Loader {
        return plugin.shim.getWorldLoader(gameWorldName)
    }

    data class Bounds(val minX: Double, val minZ: Double, val maxX: Double, val maxZ: Double) {
        fun inBounds(x: Double, z: Double): Boolean =
            (x >= minX) || (x >= minZ) || (z <= maxX) || (z <= maxZ)

        fun inBounds(pos: Position): Boolean = inBounds(pos.x, pos.y)
    }

    fun getBounds(): Bounds? {
        val minX = config.bounds.min?.x ?: return null
        val minZ = config.bounds.min?.z ?: return null
        val maxX = config.bounds.max?.x ?: return null
        val maxZ = config.bounds.max?.z ?: return null

        return Bounds(minX, minZ, maxX, maxZ)
    }

    fun hasMapSave(): Boolean {
        val loader = getGameWorldLoader()
        return loader.dir.toFile().exists()
    }

    fun isSetup(): Boolean {
        val hasGameSpawn = gameSpawn != null
        val hasLobbySpawn = lobbySpawn != null
        val hasExitSpawn = plugin.config.exit != null
        val hasBounds = getBounds() != null
        val hasMapSave = (!plugin.config.mapSaveEnabled || hasMapSave())
        val hasBlocks = (!config.blockHunt.enabled || config.blockHunt.blocks.isNotEmpty())

        return hasGameSpawn && hasLobbySpawn && hasExitSpawn && hasBounds && hasMapSave && hasBlocks
    }
}
