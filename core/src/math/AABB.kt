package cat.freya.khs.math

data class AABB(val min: Vector, val max: Vector) {

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
