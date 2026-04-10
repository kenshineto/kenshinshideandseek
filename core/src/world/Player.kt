package cat.freya.khs.world

import cat.freya.khs.disguise.Disguise
import cat.freya.khs.game.Board
import cat.freya.khs.math.Vector
import cat.freya.khs.type.Material
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.player.ClientVersion
import com.github.retrooper.packetevents.wrapper.PacketWrapper

/** Represents a current online player on the minecraft server */
interface Player : Entity {
    val name: String

    /**
     * @return the internal type of this player Can be turned back into a [Player] with wrapPlayer
     *   on the shim
     */
    fun getHandle(): Any

    /** @return the client version */
    fun getClientVersion(): ClientVersion {
        return PacketEvents.getAPI().playerManager.getClientVersion(getHandle())
    }

    /** Get the current health of the player */
    fun getHealth(): Double

    /** Set the players current health */
    fun setHealth(health: Double)

    /** Damage the player by some amount */
    fun damage(amount: Double) {
        setHealth(maxOf(0.0, getHealth() - amount))
    }

    /** Heal the player to max health */
    fun heal()

    /** Get the current food level of the player */
    fun getHunger(): UInt

    /** Satiate the player to be fully fed */
    fun satiate()

    /** Send the player flying */
    fun knockBack(direction: Vector)

    /** @return if the player is allowed to fly */
    fun getAllowedFlight(): Boolean

    /** Set if the player is allowed to fly */
    fun setAllowedFlight(allowedFlight: Boolean)

    /** @return if the player is currently flying */
    fun getFlying(): Boolean

    /** Set if the player is currently flying */
    fun setFlying(flying: Boolean)

    /** Get the players current inventory */
    fun getInventory(): PlayerInventory

    /** Show the player a custom inventory */
    fun showInventory(inv: Inventory)

    /** Close the players currently open inventory (if exists) */
    fun closeInventory()

    /** Send the player a chat message */
    fun message(message: String)

    /** Send the player a status in their action bar */
    fun actionBar(message: String)

    /** Send the player a title and subtitle */
    fun title(title: String, subTitle: String)

    /** Play a sound for the player */
    fun playSound(sound: String, volume: Double, pitch: Double)

    /** Disguise the player as a block material */
    fun createDisguise(material: Material): Disguise?

    /** Get how much damage the player's equipped item can do */
    fun getAttackDamage(): Double

    /** @return the position of where the player is looking from */
    fun getEyePosition(): Location

    /** @return the direction the player is looking */
    fun getEyeDirection(): Vector

    /**
     * Ray cast to see how far the player can reach (attack)
     *
     * @return double on hit, null if no hit
     */
    fun getReach(maxReach: Double): Double?

    enum class GameMode {
        CREATIVE,
        SURVIVAL,
        ADVENTURE,
        SPECTATOR,
    }

    /** @return the player's game mode */
    fun getGameMode(): GameMode

    /** Set the players game mode */
    fun setGameMode(gameMode: GameMode)

    /** Check if the player has a given permission string */
    fun hasPermission(permission: String): Boolean

    /** @return the players currently active scoreboard */
    fun getScoreBoard(): Board

    /** Change the players active score board When given null, it should set the main score board */
    fun setScoreBoard(board: Board?)

    /**
     * Spawn a taunt (usually a firework), at the location of the player to give away their location
     */
    fun taunt()

    /** Send the player a custom nms packet */
    fun sendPacket(packet: PacketWrapper<*>)
}
