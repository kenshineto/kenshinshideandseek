package cat.freya.khs.mod

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
import java.nio.file.Path
import java.util.UUID

object ModLogger : KhsShim.Logger {
    private val logger: Logger? = LoggerFactory.getLogger(KhsMod.ID)

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

class ModKhsShim(val mod: KhsMod) : AbstractKhsShim(mod.info.platform) {
    override val pluginVersion: String = mod.info.pluginVersion

    override val serverVersion: String
        get() = mod.server.inner.serverVersion

    override val logger: KhsShim.Logger = ModLogger

    override val dataDirectory: Path
        get() =
            mod.server.inner.serverDirectory
                .resolve("config")
                .resolve(KhsMod.ID)

    override fun getMaterials(): List<ModMaterial> {
        return getBlocks() + getItems()
    }

    override fun getBlocks(): List<ModBlockMaterial> {
        return BuiltInRegistries.BLOCK.map { block ->
            val id = BuiltInRegistries.BLOCK.getKey(block)
            val key = ResourceKey.create(Registries.BLOCK, id)
            val holder = BuiltInRegistries.BLOCK.get(key).get()
            ModBlockMaterial(holder, key)
        }
    }

    private fun getItems(): List<ModItemMaterial> {
        return BuiltInRegistries.ITEM.map { item ->
            val id = BuiltInRegistries.ITEM.getKey(item)
            val key = ResourceKey.create(Registries.ITEM, id)
            val holder = BuiltInRegistries.ITEM.get(key).get()
            ModItemMaterial(holder, key)
        }
    }

    override fun parseMaterial(platformKey: String): ModMaterial? {
        return ModMaterial.parse(platformKey)
    }

    override fun parseItem(itemConfig: ItemConfig): ModItem? {
        return ModItem.parse(mod.server, itemConfig)
    }

    override fun parseEffect(effectConfig: EffectConfig): ModEffect? {
        return ModEffect.parse(effectConfig)
    }

    override fun getPlayers(): List<ModPlayer> {
        return mod.server.getPlayers()
    }

    override fun getPlayer(uuid: UUID): ModPlayer? {
        return mod.server.getPlayer(uuid)
    }

    override fun getPlayer(name: String): ModPlayer? {
        return mod.server.getPlayer(name)
    }

    override fun wrapPlayer(inner: Any?): ModPlayer? {
        val player = inner as? ServerPlayer ?: return null
        return ModPlayer(mod, player)
    }

    override fun sendPlayerToServer(uuid: UUID, server: String): Boolean {
        // TODO:
        return false
    }

    override fun getWorldNames(): List<String> {
        val container = mod.server.getWorldContainer()
        val namespaces = container.toFile().listFiles() ?: emptyArray()
        return namespaces
            .map { namespace ->
                val dirs = namespace.listFiles() ?: emptyArray()
                dirs
                    .filter { it.isDirectory }
                    .map { "${namespace.name}:${it.name}" }
            }.flatten()
    }

    override fun getWorld(worldName: String): ModWorld? {
        return mod.server.getWorld(worldName)
    }

    override fun getWorldLoader(worldName: String): ModWorldLoader {
        return ModWorldLoader(mod, worldName)
    }

    override fun createWorld(worldName: String, type: World.Type): ModWorld? {
        val level = ModWorld.createLevel(mod, worldName, type) ?: return null
        return ModWorld(mod, level)
    }

    override fun createInventory(title: String, size: UInt): ModInventory {
        val inv = ModContainer(size, title)
        return ModInventory(mod.shim, inv)
    }

    override fun getBoard(name: String): ModBoard {
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
