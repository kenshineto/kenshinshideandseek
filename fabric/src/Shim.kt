package cat.freya.khs.fabric

import cat.freya.khs.AbstractKhsShim
import cat.freya.khs.KhsShim
import cat.freya.khs.config.EffectConfig
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.Board as KhsBoard
import cat.freya.khs.player.Inventory as KhsInventory
import cat.freya.khs.player.Player as KhsPlayer
import cat.freya.khs.world.Effect as KhsEffect
import cat.freya.khs.world.Item as KhsItem
import cat.freya.khs.world.Material as KhsMaterial
import cat.freya.khs.world.World as KhsWorld
import java.nio.file.Path
import java.util.UUID
import net.minecraft.server.level.ServerPlayer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FabricLogger(mod: KhsMod) : KhsShim.Logger {
    private val logger: Logger? = LoggerFactory.getLogger(mod.ID)

    override fun info(message: String) {
        logger?.info(message)
    }

    override fun warning(message: String) {
        logger?.warn(message)
    }

    override fun error(message: String) {
        logger?.error(message)
    }
}

class FabricKhsShim(val mod: KhsMod) : AbstractKhsShim("Fabric") {

    override val pluginVersion: String = mod.container.metadata?.version?.friendlyString ?: "null"

    override val mcVersionString: String =
        mod.loader.getModContainer("minecraft").get().metadata.version.friendlyString

    override val logger: KhsShim.Logger = FabricLogger(mod)

    override val players: List<KhsPlayer>
        get() = mod.server.getPlayers()

    override val worlds: List<String> = TODO("SET VALUE")

    override val blocks: List<String> = TODO("SET VALUE")

    override val dataDirectory: Path = mod.loader.configDir.resolve(mod.ID)

    override fun parseMaterial(materialName: String): KhsMaterial? = error("todo")

    override fun parseItem(itemConfig: ItemConfig): KhsItem? = error("todo")

    override fun parseEffect(effectConfig: EffectConfig): KhsEffect? = error("todo")

    override fun getPlayer(uuid: UUID): KhsPlayer? = mod.server.getPlayer(uuid)

    override fun getPlayer(name: String): KhsPlayer? = mod.server.getPlayer(name)

    override fun wrapPlayer(inner: Any?): KhsPlayer? {
        val player = inner as? ServerPlayer ?: return null
        return FabricKhsPlayer(mod, player)
    }

    override fun getWorld(worldName: String): KhsWorld? = mod.server.getWorld(worldName)

    override fun getWorldLoader(worldName: String): KhsWorld.Loader =
        FabricKhsWorldLoader(mod, worldName)

    override fun createWorld(worldName: String, type: KhsWorld.Type): KhsWorld? = error("todo")

    override fun createInventory(title: String, size: UInt): KhsInventory? {
        val inv = FabricContainer(size, title)
        return FabricKhsInventory(mod.shim, inv)
    }

    override fun getBoard(name: String): KhsBoard? = error("todo")

    override fun broadcast(message: String) {
        mod.server.getPlayers().forEach { it.message(message) }
    }

    override fun disable() {
        mod.onShutdown()
    }

    override fun scheduleEvent(ticks: ULong, event: () -> Unit) {
        mod.server.scheduleTask(event, ticks)
    }
}
