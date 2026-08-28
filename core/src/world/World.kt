package cat.freya.khs.world

import java.nio.file.Path

/**
 * The prefix of all world names that are to be treated as map saves
 *
 * hs_ (map save v1) was named after the world name, not the map name, not allowing multiple names per map
 *
 * hs2_ (map save v2) is named after the map name
 */
const val MAP_SAVE_PREFIX = "hs2_"

/** Represents a minecraft world */
interface World {
    val name: String

    /** Represents the type of world/dimension that this world is */
    enum class Type {
        NORMAL,
        FLAT,
        NETHER,
        END,
        UNKNOWN,
    }

    /** The type of world/dimension that this world is */
    val type: Type

    /** Represents a minecraft world border */
    interface Border {
        val x: Double
        val z: Double
        val size: Double

        /** Recenter and resize the world border */
        fun move(newX: Double, newZ: Double, newSize: ULong, delay: ULong)

        /** Resize the world border */
        fun move(newSize: ULong, delay: ULong) {
            move(x, z, newSize, delay)
        }

        /** Reset the world border do its original size */
        fun reset() {
            move(0.0, 0.0, 30_000_000UL, 0UL)
        }
    }

    /** The world's world border */
    val border: Border

    /** Where in this world is the default spawn location */
    fun getSpawn(): Location

    /** Play a sound at the given location */
    fun playSound(position: Position, sound: String, volume: Double, pitch: Double)

    /**
     * Unload this world
     *
     * WARNING: The handle to this object may stil exist even if the world is unloaded
     */
    fun unload()
}

data class WorldInfo(
    /** The name of the world */
    val name: String,
    /** The directory this world is stayed in */
    val dir: Path,
)

fun isMapSave(worldName: String): Boolean {
    return worldName.startsWith(MAP_SAVE_PREFIX)
}
