package cat.freya.khs

import cat.freya.khs.config.*
import cat.freya.khs.game.Board
import cat.freya.khs.game.Game
import cat.freya.khs.game.KhsMap
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
import cat.freya.khs.world.World.AbstractLoader
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.PacketEventsAPI
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.mockito.Answers
import org.mockito.Mockito

private object TestLogger : KhsShim.Logger {
    override fun info(message: String) = println(message)

    override fun warning(message: String) = println(message)

    override fun error(message: String) = println(message)
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

class TestWorldLoader(val shim: TestShim, name: String) : World.AbstractLoader(name, name, shim.dataDirectory) {
    override fun load() = shim.getWorld(name)

    override fun unload() {}
}

class TestWorld(shim: TestShim, override val name: String) : AbstractWorld(shim) {
    override val type = World.Type.NORMAL
    override val border = TestBorder()
    override val loader = shim.getWorldLoader(name)

    override fun getSpawn() = Location(worldName = name)

    override fun playSound(position: Position, sound: String, volume: Double, pitch: Double) {}
}

class TestMaterial(val name: String) : Material {
    override val key = ResourceKey(name, null, name)
    override val isBlock = true
    override val isItem = true
}

class TestItem(override val config: ItemConfig) : Item {
    override val name = config.material
    override val material = TestMaterial(name)
}

open class TestInventory(override val title: String?, val size: UInt) : Inventory {
    private val items = MutableList<Item?>(size.toInt()) { null }

    override fun get(index: UInt): Item? {
        if (index >= size) return null
        return items.get(index.toInt())
    }

    override fun set(index: UInt, item: Item?) {
        if (index >= size) return
        items[index.toInt()] = item
    }

    override fun remove(item: Item) {
        for (i in 0u until size) {
            if (get(i) == item) set(i, null)
        }
    }

    override fun getContents(): List<Item?> {
        return items
    }

    override fun setContents(contents: List<Item?>) {
        contents.forEachIndexed { i, item ->
            set(i.toUInt(), item)
        }
    }

    override fun clearContents() {
        items.fill(null)
    }
}

class TestPlayerInventory(title: String?, size: UInt) : TestInventory(title, size), PlayerInventory {
    // helmet
    private var helmet: Item? = null

    override fun getHelmet() = helmet

    override fun setHelmet(helmet: Item?) {
        this.helmet = helmet
    }

    // chestplate
    private var chestplate: Item? = null

    override fun getChestplate() = chestplate

    override fun setChestplate(chestplate: Item?) {
        this.chestplate = chestplate
    }

    // leggings
    private var leggings: Item? = null

    override fun getLeggings() = leggings

    override fun setLeggings(leggings: Item?) {
        this.leggings = leggings
    }

    // boots
    private var boots: Item? = null

    override fun getBoots() = boots

    override fun setBoots(boots: Item?) {
        this.boots = boots
    }
}

object TestTeam : Board.Team {
    override fun setPrefix(prefix: String) {}

    override fun setCanCollide(canCollide: Boolean) {}

    override fun setNameTagsVisible(nameTagsVisible: Boolean) {}

    override fun setPlayers(players: Set<UUID>) {}
}

object TestBoard : Board {
    override fun getTeam(name: String) = TestTeam

    override fun setText(title: String, text: List<String>) {}
}

class TestPlayer(val shim: TestShim, override val name: String, override val uuid: UUID) : Player {
    override fun getHandle() = this

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
    private var location = Location(worldName = "world")

    override fun getLocation() = location

    override fun teleport(location: Location?) {
        this.location = location ?: this.location
    }

    override fun getWorld() = shim.getWorld(location.worldName)

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

    override fun getGameMode() = gameMode

    override fun setGameMode(gameMode: Player.GameMode) {
        this.gameMode = gameMode
    }

    // inventory
    private val inventory = TestPlayerInventory(name, 36u)

    override fun getInventory() = inventory

    override fun showInventory(inv: Inventory) {}

    // scoreboard
    override fun getScoreBoard() = TestBoard

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

    override fun getPitch() = 0f

    override fun getReach(maxReach: Double) = 4.5

    override fun getVelocity() = Vector()

    override fun getYaw() = 0f

    override fun giveEffect(effect: Effect) {}

    override fun hasPermission(permission: String) = true

    override fun isAlive() = health >= 0.5

    override fun knockBack(direction: Vector) {}

    override fun message(message: String) {}

    override fun playSound(sound: String, volume: Double, pitch: Double) {}

    override fun setCollides(collides: Boolean) {}

    override fun setSpeed(amplifier: UInt) {}

    override fun taunt() {}

    override fun title(title: String, subTitle: String) {}
}

abstract class TestShim : AbstractKhsShim("test") {
    override val logger: KhsShim.Logger = TestLogger

    // player
    override fun getPlayers() = emptyList<TestPlayer>()

    override fun getPlayer(uuid: UUID) = getPlayers().firstOrNull { it.uuid == uuid }

    override fun getPlayer(name: String) = getPlayers().firstOrNull { it.name == name }

    override fun wrapPlayer(inner: Any?) = inner as? Player

    // world
    override fun getWorldNames() = emptyList<String>()

    override fun getWorld(worldName: String) = TestWorld(this, worldName)

    override fun getWorldLoader(worldName: String) = TestWorldLoader(this, worldName)

    override fun createWorld(worldName: String, type: World.Type) = getWorld(worldName)

    // items
    override fun parseItem(itemConfig: ItemConfig) = TestItem(itemConfig)

    override fun parseMaterial(platformKey: String) = TestMaterial(platformKey)

    // misc
    override fun createInventory(title: String, size: UInt) = TestInventory(title, size)

    override fun getBoard(name: String) = TestBoard

    override fun disable() = error("disabled :(")

    // stub values
    override val dataDirectory: Path = Paths.get("")

    // stub functions
    override fun broadcast(message: String) {}

    override fun getMaterials() = emptyList<TestMaterial>()

    override fun parseEffect(effectConfig: EffectConfig) = null

    override fun sendPlayerToServer(uuid: UUID, server: String) = false

    override fun writeConfigFile(fileName: String, content: String) {}
}

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
abstract class KhsTest(val initOnSetup: Boolean = true) : TestShim() {
    override val serverVersion: String = "26.2"

    val config = KhsConfig()
    val itemsConfig = KhsItemsConfig()
    val mapsConfig = KhsMapsConfig()
    val boardConfig = KhsBoardConfig()
    val locale = KhsLocale()

    val plugin = Khs(this)
    val game = plugin.game

    val alice = TestPlayer(this, "alice", UUID(1L, 1L))
    val bob = TestPlayer(this, "bob", UUID(2L, 2L))
    val eve = TestPlayer(this, "eve", UUID(3L, 3L))
    val mallory = TestPlayer(this, "mallory", UUID(4L, 4L))
    val world = TestWorld(this, "world")

    @BeforeEach
    fun setup() {
        config.database.type = DatabaseType.DISABLED
        config.mapSaveEnabled = false

        PacketEvents.setAPI(Mockito.mock(PacketEventsAPI::class.java, Answers.RETURNS_DEEP_STUBS))

        if (initOnSetup) {
            plugin.init()
        }
    }

    override fun getPlayers(): List<TestPlayer> {
        return listOf(alice, bob, eve, mallory)
    }

    override fun readConfigFile(fileName: String): InputStream? {
        return when (fileName) {
            "config.yml" -> serialize(config)
            "items.yml" -> serialize(itemsConfig)
            "maps.yml" -> serialize(mapsConfig)
            "board.yml" -> serialize(boardConfig)
            "locale.yml" -> serialize(locale)
            else -> null
        }?.byteInputStream()
    }

    override fun scheduleEvent(ticks: ULong, event: () -> Unit) {
        // FIXME: aaaahhhhh
    }

    fun setupMap(): KhsMap {
        // setup map
        val gameSpawn = Position(1.0, 1.0, 1.0)
        val lobbySpawn = Position(2.0, 2.0, 2.0)
        val seekerLobbySpawn = Position(3.0, 3.0, 3.0)
        val spawns = SpawnsConfig(gameSpawn, lobbySpawn, seekerLobbySpawn)
        val bounds = BoundsConfig(BoundConfig(-10.0, -10.0), BoundConfig(10.0, 10.0))
        val mapConfig = MapConfig(world.name, spawns, bounds)
        mapsConfig.maps = mapOf("map" to mapConfig)
        config.exit = Location(worldName = world.name)

        plugin.reloadConfig()
        val map = plugin.maps.get("map")
        assertNotNull(plugin.maps.get("map"))
        assert(isMapSetup())

        return map!!
    }

    fun isMapSetup(): Boolean {
        return plugin.maps.get("map")?.isSetup() == true
    }

    fun skipTicks(ticks: ULong) {
        for (i in 0UL until ticks) {
            plugin.doTick()
        }
    }

    fun skipSeconds(seconds: ULong) {
        skipTicks(seconds * 20UL)
    }

    fun skipToStatusWithin(status: Game.Status, withinSeconds: ULong) {
        var timer = withinSeconds
        do {
            skipSeconds(1UL)
            assertNotEquals(0UL, --timer, "game status did not change to ${status} within ${withinSeconds} seconds")
        } while (game.status != status)
    }

    fun skipToStatus(status: Game.Status) {
        // 30 min default
        skipToStatusWithin(status, 30UL * 60UL)
    }

    fun assertThrows(fn: () -> Unit) {
        assertThrows(Throwable::class.java, fn)
    }

    fun assertStatus(status: Game.Status) {
        assertEquals(status, game.status)
    }
}
