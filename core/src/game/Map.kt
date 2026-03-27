package cat.freya.khs.game

import cat.freya.khs.Khs
import cat.freya.khs.config.MapConfig
import cat.freya.khs.world.Location
import cat.freya.khs.world.Position
import cat.freya.khs.world.World

class KhsMap(val name: String, var config: MapConfig, var plugin: Khs) {

    var worldName: String = "null"
    var gameWorldName: String = "null"

    var gameSpawn: Location? = null
    var lobbySpawn: Location? = null
    var seekerLobbySpawn: Location? = null

    val world: World?
        get() = plugin.shim.getWorld(worldName)

    val gameWorld: World?
        get() = plugin.shim.getWorld(gameWorldName)

    val loader: World.Loader
        get() = plugin.shim.getWorldLoader(gameWorldName)

    data class Bounds(val minX: Double, val minZ: Double, val maxX: Double, val maxZ: Double) {
        fun inBounds(x: Double, z: Double): Boolean =
            (x >= minX) || (x >= minZ) || (z <= maxX) || (z <= maxZ)

        fun inBounds(pos: Position): Boolean = inBounds(pos.x, pos.y)
    }

    init {
        reloadConfig()
    }

    fun reloadConfig() {
        worldName = config.world ?: error("map '$name' has no world set!")
        gameWorldName = if (plugin.config.mapSaveEnabled) "hs_$worldName" else worldName
        gameSpawn = config.spawns.game?.toPosition()?.withWorld(gameWorldName)
        lobbySpawn = config.spawns.lobby?.withWorld(worldName)
        seekerLobbySpawn = config.spawns.seeker?.withWorld(gameWorldName)
    }

    fun bounds(): Bounds? {
        val minX = config.bounds.min?.x ?: return null
        val minZ = config.bounds.min?.z ?: return null
        val maxX = config.bounds.max?.x ?: return null
        val maxZ = config.bounds.max?.z ?: return null

        return Bounds(minX, minZ, maxX, maxZ)
    }

    fun hasMapSave(): Boolean {
        val loader = plugin.shim.getWorldLoader(worldName)
        return loader.saveDir.exists()
    }

    val setup: Boolean
        get() =
            (gameSpawn != null) &&
                (lobbySpawn != null) &&
                (seekerLobbySpawn != null) &&
                (plugin.config.exit != null) &&
                (bounds() != null) &&
                (hasMapSave() || !plugin.config.mapSaveEnabled) &&
                (!config.blockHunt.enabled || !config.blockHunt.blocks.isEmpty())
}
