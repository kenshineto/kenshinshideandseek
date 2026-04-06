package cat.freya.khs.bukkit

import cat.freya.khs.AbstractKhsShim
import cat.freya.khs.KhsShim
import cat.freya.khs.config.EffectConfig
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.world.World
import com.google.common.io.ByteStreams
import java.io.File
import java.nio.file.Path
import java.util.UUID
import org.bukkit.ChatColor
import org.bukkit.WorldCreator
import org.bukkit.WorldType

class BukkitLogger(val plugin: KhsPlugin) : KhsShim.Logger {
    override fun info(message: String) = plugin.logger.info(message)

    override fun warning(message: String) = plugin.logger.warning(message)

    override fun error(message: String) = plugin.logger.severe(message)
}

class BukkitKhsShim(val plugin: KhsPlugin) : AbstractKhsShim("Bukkit") {
    override val pluginVersion: String = plugin.description.version

    override val serverVersion: String =
        Regex("""MC:\s*([\d.]+)""").find(plugin.server.version)?.groupValues?.get(1)
            ?: error("failed to parse mc version")

    override val logger = BukkitLogger(plugin)

    override val dataDirectory: Path = plugin.dataFolder.toPath()

    override fun getMaterials(): List<BukkitMaterial> {
        return org.bukkit.Material.entries.map { BukkitMaterial(it) }
    }

    override fun getPlayers(): List<BukkitPlayer> {
        return plugin.server.onlinePlayers.map { BukkitPlayer(plugin, it) }
    }

    override fun parseMaterial(platformKey: String): BukkitMaterial? {
        return BukkitMaterial.parse(platformKey)
    }

    override fun parseItem(itemConfig: ItemConfig?): BukkitItem? {
        if (itemConfig == null) return null
        return BukkitItem.parse(itemConfig)
    }

    override fun parseEffect(effectConfig: EffectConfig?): BukkitEffect? {
        if (effectConfig == null) return null
        return BukkitEffect.parse(effectConfig)
    }

    override fun getPlayer(uuid: UUID): BukkitPlayer? {
        val player = plugin.server.getPlayer(uuid) ?: return null
        return BukkitPlayer(plugin, player)
    }

    override fun getPlayer(name: String): BukkitPlayer? {
        val player = plugin.server.getPlayer(name) ?: return null
        return BukkitPlayer(plugin, player)
    }

    override fun wrapPlayer(inner: Any?): BukkitPlayer? {
        val player = inner as? org.bukkit.entity.Player ?: return null
        return BukkitPlayer(plugin, player)
    }

    override fun sendPlayerToServer(uuid: UUID, server: String): Boolean {
        val player = plugin.server.getPlayer(uuid) ?: return false
        val out = ByteStreams.newDataOutput()
        out.writeUTF("Connect")
        out.writeUTF(plugin.khs.config.leaveServer)
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray())

        // TODO: check if there was an error...
        return true
    }

    override fun getWorldNames(): List<String> {
        return plugin.server.worldContainer
            .listFiles()
            .filter {
                if (!it.isDirectory) return@filter false

                val session = File(it, "session.lock")
                val level = File(it, "level.dat")

                session.exists() && level.exists()
            }
            .map { it.name }
    }

    override fun getWorld(worldName: String): BukkitWorld? {
        val world = plugin.server.getWorld(worldName) ?: return null
        return BukkitWorld(this, world)
    }

    override fun getWorldLoader(worldName: String): BukkitWorldLoader {
        return BukkitWorldLoader(plugin, worldName)
    }

    override fun createWorld(worldName: String, type: World.Type): BukkitWorld? {
        val worldType = if (type == World.Type.FLAT) WorldType.FLAT else WorldType.NORMAL
        val env =
            when (type) {
                World.Type.NETHER -> org.bukkit.World.Environment.NETHER
                World.Type.END -> org.bukkit.World.Environment.THE_END
                else -> org.bukkit.World.Environment.NORMAL
            }

        val creator = WorldCreator(worldName)
        creator.type(worldType)
        creator.environment(env)
        plugin.server.createWorld(creator)

        val world = plugin.server.getWorld(worldName) ?: return null
        world.save()

        return BukkitWorld(plugin.shim, world)
    }

    override fun createInventory(title: String, size: UInt): BukkitInventory {
        val inv = plugin.server.createInventory(null, size.toInt(), title)
        return BukkitInventory(this, inv, title)
    }

    override fun getBoard(name: String): BukkitBoard? {
        return runCatching {
                val board = plugin.server.scoreboardManager?.newScoreboard ?: return null
                return BukkitBoard(this, board)
            }
            .getOrElse { null }
    }

    override fun broadcast(message: String) {
        plugin.server.broadcastMessage(formatText(message))
    }

    override fun disable() {
        plugin.server.pluginManager.disablePlugin(plugin)
    }

    override fun scheduleEvent(ticks: ULong, event: () -> Unit) {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, event, ticks.toLong())
    }
}

/// formats &c'esc color codes to bukkits colors
fun formatText(message: String): String {
    return ChatColor.translateAlternateColorCodes('&', message)
}
