package cat.freya.khs.fabric

import cat.freya.khs.KhsShim
import cat.freya.khs.Logger
import cat.freya.khs.config.EffectConfig
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.Board as KhsBoard
import cat.freya.khs.player.Inventory as KhsInventory
import cat.freya.khs.player.Player as KhsPlayer
import cat.freya.khs.world.Effect as KhsEffect
import cat.freya.khs.world.Item as KhsItem
import cat.freya.khs.world.Material as KhsMaterial
import cat.freya.khs.world.World as KhsWorld
import java.io.File
import java.io.InputStream
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import org.slf4j.LoggerFactory

class FabricLogger(val mod: KhsMod) : Logger {
    val logger = LoggerFactory.getLogger(mod.id)

    override fun info(message: String) = logger.info(message)

    override fun warning(message: String) = logger.warn(message)

    override fun error(message: String) = logger.error(message)
}

class FabricKhsShim(val mod: KhsMod) : KhsShim {

    override val pluginVersion: String = mod.container?.metadata?.version?.friendlyString ?: "null"

    override val mcVersion: List<UInt> =
        parseMcVersion(
            mod.loader?.getModContainer("minecraft")?.getOrNull()?.metadata?.version?.friendlyString
        )

    override val platform: String = "Fabric"

    override val logger: Logger = FabricLogger(mod)

    override val players: List<KhsPlayer> = TODO("SET VALUE")

    override val worlds: List<String> = TODO("SET VALUE")

    override val blocks: List<String> = TODO("SET VALUE")

    override val sqliteDatabasePath: String
        get() = File(mod.configDir(), "khs.db").path

    override fun readConfigFile(fileName: String): InputStream? {
        val dir = mod.configDir()
        if (!dir.exists()) {
            dir.mkdirs() || error("Failed to make plugin config directory")
        }
        val file = File(dir, fileName)
        return if (file.exists()) file.inputStream() else null
    }

    override fun writeConfigFile(fileName: String, content: String) {
        val dir = mod.configDir()
        if (!dir.exists()) {
            dir.mkdirs() || error("Failed to make plugin config directory")
        }
        val file = File(dir, fileName)
        file.writeText(content)
    }

    override fun parseMaterial(materialName: String): KhsMaterial? = error("todo")

    override fun parseItem(itemConfig: ItemConfig): KhsItem? = error("todo")

    override fun parseEffect(effectConfig: EffectConfig): KhsEffect? = error("todo")

    override fun getPlayer(uuid: UUID): KhsPlayer? = error("todo")

    override fun getPlayer(name: String): KhsPlayer? = error("todo")

    override fun wrapPlayer(inner: Any?): KhsPlayer? = error("todo")

    override fun getWorld(worldName: String): KhsWorld? = error("todo")

    override fun getWorldLoader(worldName: String): KhsWorld.Loader = error("todo")

    override fun createWorld(worldName: String, type: KhsWorld.Type): KhsWorld? = error("todo")

    override fun createInventory(title: String, size: UInt): KhsInventory? = error("todo")

    override fun getBoard(name: String): KhsBoard? = error("todo")

    override fun broadcast(message: String) = error("todo")

    override fun disable() {
        mod.onShutdown()
    }

    override fun scheduleEvent(ticks: ULong, event: Function0<Unit>) = error("todo")
}
