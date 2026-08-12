package cat.freya.khs

import cat.freya.khs.config.*
import cat.freya.khs.game.Board
import cat.freya.khs.game.Game
import cat.freya.khs.game.gamemode.GameMode
import cat.freya.khs.math.Vector
import cat.freya.khs.menu.Inventory
import cat.freya.khs.menu.PlayerInventory
import cat.freya.khs.type.Effect
import cat.freya.khs.type.Item
import cat.freya.khs.type.Material
import cat.freya.khs.type.ResourceKey
import cat.freya.khs.world.AbstractWorld
import cat.freya.khs.world.Location
import cat.freya.khs.world.Player
import cat.freya.khs.world.Position
import cat.freya.khs.world.World
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestReporter

class TestLogger(val reporter: TestReporter) : KhsShim.Logger {
    override fun info(message: String) = reporter.publishEntry("info", message)

    override fun warning(message: String) = reporter.publishEntry("warning", message)

    override fun error(message: String) = reporter.publishEntry("error", message)
}

class TestBorder : World.Border {
    override var x: Double = 0.0
    override var z: Double = 0.0
    override var size: Double = 30_000_000.0

    override fun move(newX: Double, newZ: Double, newSize: ULong, delay: ULong) {
        x = newX
        z = newZ
        size = newSize.toDouble()
    }
}

class TestWorldLoader(override val name: String, val shim: TestShim) : World.Loader {
    override val isMapSave = false
    override val dir = Paths.get("")

    override fun load() = shim.getWorld(name)

    override fun unload() {}

    override fun rollback() {}
}

class TestWorld(override val name: String, shim: TestShim) : AbstractWorld(shim) {
    override val type = World.Type.NORMAL
    override val border = TestBorder()
    override val loader = shim.getWorldLoader(name)

    override fun getSpawn() = Location(worldName = name)

    override fun playSound(position: Position, sound: String, volume: Double, pitch: Double) {}
}

// TODO: acutally get and set items
class TestInventory(override val title: String?) : PlayerInventory {
    override fun getHelmet(): Item? = null

    override fun getChestplate(): Item? = null

    override fun getLeggings(): Item? = null

    override fun getBoots(): Item? = null

    override fun setHelmet(helmet: Item?) {}

    override fun setChestplate(chestplate: Item?) {}

    override fun setLeggings(leggings: Item?) {}

    override fun setBoots(boots: Item?) {}

    override fun get(index: UInt): Item? = null

    override fun set(index: UInt, item: Item?) {}

    override fun remove(item: Item) {}

    override fun getContents(): List<Item?> = emptyList()

    override fun setContents(contents: List<Item?>) {}

    override fun clearContents() {}
}

object TestTeam : Board.Team {
    override fun setPrefix(prefix: String) {}

    override fun setCanCollide(canCollide: Boolean) {}

    override fun setNameTagsVisible(nameTagsVisible: Boolean) {}

    override fun setPlayers(players: Set<UUID>) {}
}

object TestBoard : Board {
    override fun getTeam(name: String): Board.Team = TestTeam

    override fun setText(title: String, text: List<String>) {}
}

class TestPlayer(val shim: TestShim, override val name: String, override val uuid: UUID) : Player {
    override fun getHandle(): Any = this

    override val type = ResourceKey("minecraft:player", null, "minecraft:player")

    // health
    private var health = 20.0

    override fun getHealth() = health

    override fun setHealth(health: Double) {
        this.health = health
    }

    override fun heal() = setHealth(20.0)

    // hunger
    private var hunger = 20u

    override fun getHunger() = hunger

    override fun satiate() {
        hunger = 20u
    }

    // location
    private var location = Location(worldName = shim.theWorld.name)

    override fun getLocation() = location

    override fun teleport(location: Location?) {
        this.location = location ?: this.location
    }

    override fun getWorld(): World? = shim.theWorld

    // allowed flight
    private var allowedFlight = false

    override fun getAllowedFlight() = allowedFlight

    override fun setAllowedFlight(allowedFlight: Boolean) {
        this.allowedFlight = allowedFlight
    }

    // flying
    private var flying = false

    override fun getFlying() = flying

    override fun setFlying(flying: Boolean) {
        this.flying = flying
    }

    // game mode
    private var gameMode = Player.GameMode.SURVIVAL

    override fun getGameMode(): Player.GameMode = gameMode

    override fun setGameMode(gameMode: Player.GameMode) {
        this.gameMode = gameMode
    }

    // inventory
    private val inventory = TestInventory(name)

    override fun getInventory(): PlayerInventory = inventory

    override fun showInventory(inv: Inventory) {}

    // scoreboard
    override fun getScoreBoard(): Board = TestBoard

    override fun setScoreBoard(board: Board?) {}

    // stub values
    override val entityId: Int = 0

    // stub functions
    override fun actionBar(message: String) {}

    override fun clearEffects() {}

    override fun closeInventory() {}

    override fun createDisguise(material: Material) = null

    override fun destroy() {}

    override fun getAttackDamage() = 5.0

    override fun getEyeDirection() = Vector()

    override fun getEyePosition() = getLocation()

    override fun getHeadYaw() = 0f

    override fun getPitch(): Float = 0f

    override fun getReach(maxReach: Double) = 4.5

    override fun getVelocity() = Vector()

    override fun getYaw(): Float = 0f

    override fun giveEffect(effect: Effect) {}

    override fun hasPermission(permission: String) = true

    override fun isAlive() = health >= 0.5

    override fun knockBack(direction: Vector) {}

    override fun message(message: String) {}

    override fun playSound(sound: String, volume: Double, pitch: Double) {}

    override fun sendPacket(packet: PacketWrapper<*>) {}

    override fun setCollides(collides: Boolean) {}

    override fun setSpeed(amplifier: UInt) {}

    override fun taunt() {}

    override fun title(title: String, subTitle: String) {}
}

class TestShim(val test: KhsTest, val reporter: TestReporter) : AbstractKhsShim("test") {
    // the world
    val theWorld = TestWorld("world", this)

    // players
    val alice = TestPlayer(this, "alice", UUID(0L, 0L))
    val bob = TestPlayer(this, "bob", UUID(0L, 1L))
    val carol = TestPlayer(this, "carol", UUID(0L, 2L))

    override val serverVersion: String = "26.2"

    override val logger: KhsShim.Logger = TestLogger(reporter)

    // player
    override fun getPlayers(): List<Player> = listOf(alice, bob, carol)

    override fun getPlayer(uuid: UUID): Player? = getPlayers().firstOrNull { it.uuid == uuid }

    override fun getPlayer(name: String): Player? = getPlayers().firstOrNull { it.name == name }

    override fun wrapPlayer(inner: Any?): Player? = inner as? Player

    // world
    override fun getWorldNames(): List<String> = listOf(theWorld.name)

    override fun getWorld(worldName: String): World? = TestWorld(worldName, this)

    override fun getWorldLoader(worldName: String): World.Loader = TestWorldLoader(worldName, this)

    override fun createWorld(worldName: String, type: World.Type): World? = TestWorld(worldName, this)

    // load our test configs
    override fun readConfigFile(fileName: String): InputStream? {
        return test.readConfigFile(fileName)?.byteInputStream()
    }

    // misc
    override fun createInventory(title: String, size: UInt): Inventory? = TestInventory(title)

    override fun getBoard(name: String): Board? = TestBoard

    override fun disable() = error("disabled :(")

    // stub values
    override val dataDirectory: Path = Paths.get("")

    // stub functions
    override fun broadcast(message: String) {}

    override fun getBlocks(): List<Material> = emptyList()

    override fun getMaterials(): List<Material> = emptyList()

    override fun parseEffect(effectConfig: EffectConfig): Effect? = null

    override fun parseItem(itemConfig: ItemConfig): Item? = null

    override fun parseMaterial(platformKey: String): Material? = null

    override fun sendPlayerToServer(uuid: UUID, server: String): Boolean = false

    override fun writeConfigFile(fileName: String, content: String) {}

    // FIXME: aaaahhhhh
    override fun scheduleEvent(ticks: ULong, event: () -> Unit) {}
}

abstract class KhsTest {
    lateinit var shim: TestShim
    lateinit var plugin: Khs
    lateinit var game: Game

    lateinit var config: KhsConfig
    lateinit var itemsConfig: KhsItemsConfig
    lateinit var mapsConfig: KhsMapsConfig
    lateinit var boardConfig: KhsBoardConfig
    lateinit var locale: KhsLocale

    @BeforeEach
    fun setup(reporter: TestReporter) {
        // load configs
        config = KhsConfig()
        itemsConfig = KhsItemsConfig()
        mapsConfig = KhsMapsConfig()
        boardConfig = KhsBoardConfig()
        locale = KhsLocale()

        // modify configs
        config.database.type = DatabaseType.DUMMY

        // load plugin
        shim = TestShim(this, reporter)
        plugin = Khs(shim)
        game = plugin.game
    }

    fun readConfigFile(fileName: String): String? {
        return when (fileName) {
            "config.yml" -> serialize(config)
            "items.yml" -> serialize(itemsConfig)
            "maps.yml" -> serialize(mapsConfig)
            "board.yml" -> serialize(boardConfig)
            "locale.yml" -> serialize(locale)
            else -> null
        }
    }
}
