package cat.freya.khs.world

import cat.freya.khs.config.LegacyPosition
import cat.freya.khs.math.Vector
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.util.Vector3i
import kotlin.math.pow
import kotlin.math.sqrt

data class Position(var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0, var yaw: Float = 0f, var pitch: Float = 0f) {
    /** @return the 3d distance between this and other */
    fun distance(other: Position): Double {
        val dx = this.x - other.x
        val dy = this.y - other.y
        val dz = this.z - other.z
        val distanceSquared = dx.pow(2) + dy.pow(2) + dz.pow(2)
        return sqrt(distanceSquared)
    }

    /** Convert to a [Location] given a world name */
    fun toLocation(worldName: String): Location {
        return Location(this.x, this.y, this.z, worldName, this.yaw, this.pitch)
    }

    /** Convert to a [LegacyPosition] used for deprecated config values */
    fun toLegacy(): LegacyPosition {
        return LegacyPosition(x, y, z)
    }

    fun toVector(): Vector {
        return Vector(x, y, z)
    }

    fun clone(): Position {
        return Position(x, y, z, yaw, pitch)
    }
}

fun Vector3d.toPosition() = Position(x, y, z)

fun Vector3i.toPosition() = Position(x.toDouble(), y.toDouble(), z.toDouble())
