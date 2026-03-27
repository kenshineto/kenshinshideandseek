package cat.freya.khs.player

import cat.freya.khs.world.Effect
import cat.freya.khs.world.Location
import cat.freya.khs.world.Position
import cat.freya.khs.world.World
import java.util.UUID

// Player wrapper
interface Player {
    // Metadata
    val uuid: UUID
    val name: String

    // Position
    val location: Location
    val world: World?

    // Stats
    var health: Double
    var hunger: UInt

    fun heal()

    // Flight
    var allowFlight: Boolean
    var flying: Boolean

    // Movement
    fun teleport(position: Position)

    fun teleport(location: Location)

    fun sendToServer(server: String)

    // Inventory
    val inventory: PlayerInventory

    fun showInventory(inv: Inventory)

    fun closeInventory()

    // Potions
    fun clearEffects()

    fun giveEffect(effect: Effect)

    fun setSpeed(amplifier: UInt)

    fun setGlow(target: Player, glow: Boolean)

    fun setHidden(target: Player, hidden: Boolean)

    // Messaging
    fun message(message: String)

    fun actionBar(message: String)

    fun title(title: String, subTitle: String)

    fun playSound(sound: String, volume: Double, pitch: Double)

    // Block Hunt
    fun isDisguised(): Boolean

    fun disguise(material: String)

    fun revealDisguise()

    enum class GameMode {
        CREATIVE,
        SURVIVAL,
        ADVENTURE,
        SPECTATOR,
    }

    // Other
    fun hasPermission(permission: String): Boolean

    fun setGameMode(gameMode: GameMode)

    fun hideBoards()

    fun taunt()
}
