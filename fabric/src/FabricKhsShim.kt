package cat.freya.khs.fabric

import cat.freya.khs.AbstractKhsShim
import cat.freya.khs.KhsShim
import cat.freya.khs.config.EffectConfig
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.world.World
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import java.util.UUID

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
    override val pluginVersion: String =
        mod.container.metadata
            ?.version
            ?.friendlyString ?: "null"

    override val serverVersion: String =
        mod.loader
            .getModContainer("minecraft")
            .get()
            .metadata.version.friendlyString

    override val logger: KhsShim.Logger = FabricLogger(mod)

    override val dataDirectory: Path = mod.loader.configDir.resolve(mod.ID)

    override fun getMaterials(): List<FabricMaterial> {
        return getBlocks() + getItems()
    }

    override fun getBlocks(): List<FabricBlockMaterial> {
        return BuiltInRegistries.BLOCK.map { block ->
            val id = BuiltInRegistries.BLOCK.getKey(block)
            val key = ResourceKey.create(Registries.BLOCK, id)
            val holder = BuiltInRegistries.BLOCK.get(key).get()
            FabricBlockMaterial(holder, key)
        }
    }

    private fun getItems(): List<FabricItemMaterial> {
        return BuiltInRegistries.ITEM.map { item ->
            val id = BuiltInRegistries.ITEM.getKey(item)
            val key = ResourceKey.create(Registries.ITEM, id)
            val holder = BuiltInRegistries.ITEM.get(key).get()
            FabricItemMaterial(holder, key)
        }
    }

    override fun parseMaterial(platformKey: String): FabricMaterial? {
        return FabricMaterial.parse(platformKey)
    }

    override fun parseItem(itemConfig: ItemConfig?): FabricItem? {
        if (itemConfig == null) return null
        return FabricItem.parse(mod.server, itemConfig)
    }

    override fun parseEffect(effectConfig: EffectConfig?): FabricEffect? {
        if (effectConfig == null) return null
        return FabricEffect.parse(effectConfig)
    }

    override fun getPlayers(): List<FabricPlayer> {
        return mod.server.getPlayers()
    }

    override fun getPlayer(uuid: UUID): FabricPlayer? {
        return mod.server.getPlayer(uuid)
    }

    override fun getPlayer(name: String): FabricPlayer? {
        return mod.server.getPlayer(name)
    }

    override fun wrapPlayer(inner: Any?): FabricPlayer? {
        val player = inner as? ServerPlayer ?: return null
        return FabricPlayer(mod, player)
    }

    override fun sendPlayerToServer(uuid: UUID, server: String): Boolean {
        // TODO:
        return false
    }

    override fun getWorldNames(): List<String> {
        return mod.server
            .getWorldContainer()
            .toFile()
            .listFiles()
            .map { namespace ->
                namespace
                    .listFiles()
                    .filter {
                        if (!it.isDirectory) return@filter false

                        val session = File(it, "session.lock")
                        val level = File(it, "level.dat")

                        session.exists() && level.exists()
                    }.map { "${namespace.name}:${it.name}" }
            }.flatten()
    }

    override fun getWorld(worldName: String): FabricWorld? {
        return mod.server.getWorld(worldName)
    }

    override fun getWorldLoader(worldName: String): FabricWorldLoader {
        return FabricWorldLoader(mod, worldName)
    }

    override fun createWorld(worldName: String, type: World.Type): FabricWorld? {
        // TODO:
        return null
    }

    override fun createInventory(title: String, size: UInt): FabricInventory {
        val inv = FabricContainer(size, title)
        return FabricInventory(mod.shim, inv)
    }

    override fun getBoard(name: String): FabricBoard {
        return mod.server.getScoreBoard(name)
    }

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
