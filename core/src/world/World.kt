package cat.freya.khs.world

import cat.freya.khs.KhsShim
import java.nio.file.Path

const val MAP_SAVE_PREFIX = "hs_"

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

    /**
     * The minimum Y position that a block can be placed in this world (inclusive). This also marks
     * the start of the void
     */
    val minY: Int

    /** The maximum Y position that a block can be placed in this world (inclusive) */
    val maxY: Int

    /** Represents a minecraft world border */
    interface Border {
        val x: Double
        val z: Double
        val size: Double

        /** Recenter and resize the world border */
        fun move(newX: Double, newZ: Double, newSize: ULong, delay: ULong)

        /** Resize the world border */
        fun move(newSize: ULong, delay: ULong)

        /** Reset the world border do its origional size */
        fun reset() {
            move(0.0, 0.0, 30_000_000UL, 0UL)
        }
    }

    /** The world's world border */
    val border: Border

    interface Loader {
        val name: String

        /** If this world should be treated as a "map save" */
        val isMapSave: Boolean

        /** The directory this world is stayed in */
        val dir: Path

        /** The directory the map save (if created) of this world would be stored in */
        val saveDir: Path

        /** The directory the temp map save (if created) of this world would be stored in */
        val tempSaveDir: Path

        /** @return the loaded world. If the world is already loaded, nothing happens. */
        fun load(): World?

        /** Unload's the world from the server */
        fun unload()

        /** Roll's back the world to the previous save on disk */
        fun rollback()
    }

    abstract class AbstractLoader(
        /** the name of the world */
        override val name: String,
        /** directory where all worlds/dimensions are stored */
        val worldContainer: Path,
    ) : Loader {
        override val isMapSave = name.startsWith(MAP_SAVE_PREFIX)
        override val dir: Path = worldContainer.resolve(name)
        override val saveDir: Path = worldContainer.resolve("$MAP_SAVE_PREFIX$name")
        override val tempSaveDir: Path = worldContainer.resolve("temp_$MAP_SAVE_PREFIX$name")

        override fun rollback() {
            load()
            unload()
        }
    }

    // Returns the world loader
    val loader: Loader

    /** Where in this world is the default spawn location */
    fun getSpawn(): Location
}

abstract class AbstractWorld(shim: KhsShim) : World {
    override val minY = if (shim.supports(18)) -64 else 0
    override val maxY = if (shim.supports(18)) 319 else 255
}
