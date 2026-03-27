package cat.freya.khs.world

import java.io.File

interface World {
    /// The name of the minecraft world
    val name: String
    val type: Type

    enum class Type {
        NORMAL,
        FLAT,
        NETHER,
        END,
        UNKNOWN,
    }

    // The extent of the height
    val minY: Int
    val maxY: Int

    val spawn: Position

    /// Wrapper for world border values
    interface Border {
        val x: Double
        val z: Double
        val size: Double

        fun move(newX: Double, newZ: Double, newSize: ULong, delay: ULong)

        fun move(newSize: ULong, delay: ULong)
    }

    // World border
    val border: Border

    interface Loader {
        val name: String
        val world: World?

        // Returns the world folder
        val dir: File

        // Returns the map save folder
        val saveDir: File

        // Returns the temp map save folder
        val tempSaveDir: File

        fun load()

        fun unload()

        fun rollback()
    }

    // Returns the world loader
    val loader: Loader
}
