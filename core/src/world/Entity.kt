package cat.freya.khs.world

interface Entity {
    // Position
    val location: Location
    val world: World?

    // Meta
    val entityId: Int
    val isAlive: Boolean

    // Other
    fun setCollides(collides: Boolean)

    fun setInvisible(invisible: Boolean)

    fun destroy()
}
