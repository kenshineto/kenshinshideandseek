package cat.freya.khs.player

import cat.freya.khs.disguise.Disguise
import cat.freya.khs.world.Effect
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Location
import cat.freya.khs.world.Material
import cat.freya.khs.world.Position
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import java.util.UUID

// Player wrapper
interface Player : Entity {
    // Metadata
    val uuid: UUID
    val name: String

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

    fun setHidden(target: Player, hidden: Boolean)

    // Messaging
    fun message(message: String)

    fun actionBar(message: String)

    fun title(title: String, subTitle: String)

    fun playSound(sound: String, volume: Double, pitch: Double)

    // Block Hunt
    fun createDisguise(material: Material): Disguise?

    // Other
    enum class GameMode {
        CREATIVE,
        SURVIVAL,
        ADVENTURE,
        SPECTATOR,
    }

    var gameMode: GameMode

    fun hasPermission(permission: String): Boolean

    fun hideBoards()

    fun taunt()

    fun sendPacket(packet: PacketWrapper<*>)
}
