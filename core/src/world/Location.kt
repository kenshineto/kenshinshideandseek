package cat.freya.khs.world

import cat.freya.khs.Khs
import cat.freya.khs.player.Player

data class Location(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var worldName: String = "world",
) {
    /// Returns the position from this location
    var position: Position
        get() = Position(this.x, this.y, this.z)
        set(new: Position) {
            this.x = new.x
            this.y = new.y
            this.z = new.z
        }

    /// Returns the world associated with this location
    fun getWorld(khs: Khs): World? {
        return khs.shim.getWorld(this.worldName)
    }

    fun distance(other: Location): Double {
        if (this.worldName != other.worldName) return Double.POSITIVE_INFINITY

        return position.distance(other.position)
    }

    fun teleport(player: Player) {
        player.teleport(this)
    }

    fun clone(): Location {
        return Location(x, y, z, worldName)
    }
}
