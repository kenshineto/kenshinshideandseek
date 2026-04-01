package cat.freya.khs.world

import cat.freya.khs.config.LegacyPosition
import cat.freya.khs.player.Player
import kotlin.math.pow
import kotlin.math.sqrt

data class Position(var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0) {

    fun distance(other: Position): Double {
        val dx = this.x - other.x
        val dy = this.y - other.y
        val dz = this.z - other.z
        val distanceSquared = dx.pow(2) + dy.pow(2) + dz.pow(2)
        return sqrt(distanceSquared)
    }

    fun teleport(player: Player) {
        player.teleport(this)
    }

    fun withWorld(worldName: String): Location {
        return Location(this.x, this.y, this.z, worldName)
    }

    fun toLegacy(): LegacyPosition = LegacyPosition(x, y, z, null)

    fun clone(): Position {
        return Position(x, y, z)
    }
}
