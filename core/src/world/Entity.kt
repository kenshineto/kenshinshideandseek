package cat.freya.khs.world

import cat.freya.khs.math.Vector
import cat.freya.khs.type.Effect
import cat.freya.khs.type.ResourceKey
import java.util.UUID

interface Entity {
    /** The internal minecraft entity id */
    val entityId: Int

    /** Every entity has a UUID */
    val uuid: UUID

    /** The minecraft type of this entity */
    val type: ResourceKey

    /** @return if the entity is currently alive */
    fun isAlive(): Boolean

    /** @return the location of the entity */
    fun getLocation(): Location

    /** @return the pitch of the entity */
    fun getPitch(): Float

    /** @return the yaw of the entity */
    fun getYaw(): Float

    /** @return the head yaw (if exists) */
    fun getHeadYaw(): Float?

    /** @return the velocity of the entity */
    fun getVelocity(): Vector

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

    /** Kill and remove the entity */
    fun destroy()
}
