package cat.freya.khs.world

data class Location(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var worldName: String = "world",
) {
    /**
     * @return the distance between the two locations, returning null of they are in difference
     *   worlds
     */
    fun distance(other: Location): Double? {
        if (worldName != other.worldName) return null
        return toPosition().distance(other.toPosition())
    }

    /** Convert to a [Position] */
    fun toPosition(): Position {
        return Position(x, y, z)
    }

    fun clone(): Location {
        return Location(x, y, z, worldName)
    }
}
