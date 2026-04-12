package cat.freya.khs.math

import kotlin.math.pow
import kotlin.math.sqrt

data class Vector(var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0) {
    fun add(b: Vector): Vector {
        return Vector(x + b.x, y + b.y, z + b.z)
    }

    fun subtract(b: Vector): Vector {
        return Vector(x - b.x, y - b.y, z - b.z)
    }

    fun normalize(): Vector {
        val lengthSquared = x.pow(2) + y.pow(2) + z.pow(2)
        val length = sqrt(lengthSquared)
        return Vector(x / length, y / length, z / length)
    }

    fun clone(): Vector {
        return Vector(x, y, z)
    }
}
