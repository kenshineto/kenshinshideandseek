package cat.freya.khs.world

interface Entity {
    /** The internal minecraft entity id */
    val entityId: Int

    /** @return if the entity is currently alive */
    fun isAlive(): Boolean

    /** @return the location of the entity */
    fun getLocation(): Location

    /** @return the world the entity is currently in */
    fun getWorld(): World?

    /** Teleport the entity within it's current world */
    fun teleport(position: Position?) {
        val worldName = getWorld()?.name ?: return
        teleport(position?.toLocation(worldName))
    }

    /** Teleport the entity across worlds */
    fun teleport(location: Location?)

    /** Set if this entity is allowed to collide with other entities */
    fun setCollides(collides: Boolean)

    /** Give a potion effect to the entity */
    fun giveEffect(effect: Effect)

    /** Clear all potion effects from the entity */
    fun clearEffects()

    /** Make the entity have a different base movement speed */
    fun setSpeed(amplifier: UInt)

    /** Make the entity invisible to everyone else */
    fun setInvisible(invisible: Boolean)

    /** Kill and remove the entity */
    fun destroy()
}
