package cat.freya.khs.world

import kotlin.math.pow
import kotlin.math.sqrt

data class VectorAABB(val min: Vector, val max: Vector) {

    fun rayIntersects(origin: Vector, direction: Vector): Double? {
        val t1 = (min.x - origin.x) / direction.x
        val t2 = (max.x - origin.x) / direction.x
        val t3 = (min.y - origin.y) / direction.y
        val t4 = (max.y - origin.y) / direction.y
        val t5 = (min.z - origin.z) / direction.z
        val t6 = (max.z - origin.z) / direction.z

        val tmin = maxOf(maxOf(minOf(t1, t2), minOf(t3, t4)), minOf(t5, t6))
        val tmax = minOf(minOf(maxOf(t1, t2), maxOf(t3, t4)), maxOf(t5, t6))

        if (tmax >= maxOf(0.0, tmin)) {
            return tmin
        }

        return null
    }
}

data class Vector(var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0) {

    fun add(b: Vector): Vector {
        return Vector(x + b.x, y + b.y, z + b.z)
    }

    fun subtract(b: Vector): Vector {
        return Vector(x - b.x, y - b.y, z - b.z)
    }

    fun multiply(scale: Double): Vector {
        return Vector(x * scale, y * scale, z * scale)
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
