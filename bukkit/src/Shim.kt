package cat.freya.khs.bukkit

import cat.freya.khs.AbstractKhsShim
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
import com.cryptomorin.xseries.XMaterial
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import java.io.File
import java.nio.file.Path
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.World as BukkitWorld
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.entity.Player as BukkitPlayer
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class BukkitLogger(val plugin: KhsPlugin) : KhsShim.Logger {
    override fun info(message: String) = plugin.logger.info(message)

    override fun warning(message: String) = plugin.logger.warning(message)

    override fun error(message: String) = plugin.logger.severe(message)
}

class BukkitKhsShim(val plugin: KhsPlugin) : AbstractKhsShim("Bukkit") {
    override val pluginVersion: String = plugin.description.version

    override val mcVersionString: String =
        Regex("""MC:\s*([\d.]+)""").find(plugin.server.version)?.groupValues?.get(1)
            ?: error("failed to parse mc version")

    override val logger = BukkitLogger(plugin)

    override val players: List<KhsPlayer>
        get() = plugin.server.onlinePlayers.map { BukkitKhsPlayer(plugin, it) }

    override val worlds: List<String>
        get() =
            plugin.server.worldContainer
                .listFiles()
                .filter {
                    if (!it.isDirectory) return@filter false

                    val session = File(it, "session.lock")
                    val level = File(it, "level.dat")

                    session.exists() && level.exists()
                }
                .map { it.name }

    override val blocks: List<String>
        get() = Material.entries.map { it.toString().uppercase() }

    override val dataDirectory: Path
        get() = plugin.dataFolder.toPath()

    override fun parseMaterial(materialName: String): KhsMaterial? {
        val bukkitMaterial =
            XMaterial.matchXMaterial(materialName).getOrNull()?.get() ?: return null
        val mcMaterial =
            if (bukkitMaterial.isBlock) {
                val blockData = bukkitMaterial.createBlockData()
                SpigotConversionUtil.fromBukkitBlockData(blockData)?.type?.toString()
            } else {
                SpigotConversionUtil.fromBukkitItemMaterial(bukkitMaterial)?.toString()
            } ?: return null
        return KhsMaterial(mcMaterial, bukkitMaterial.toString())
    }

    override fun parseItem(itemConfig: ItemConfig): KhsItem? {
        return parseBukkitItem(itemConfig)
    }

    override fun parseEffect(effectConfig: EffectConfig): KhsEffect? {
        @Suppress("DEPRECATION")
        val type = PotionEffectType.getByName(effectConfig.type.uppercase()) ?: return null
        val inner =
            PotionEffect(
                type,
                effectConfig.duration.toInt(),
                effectConfig.amplifier.toInt(),
                effectConfig.ambient,
                effectConfig.particles,
            )

        return BukkitKhsEffect(inner, effectConfig)
    }

    override fun getPlayer(uuid: UUID): KhsPlayer? {
        return plugin.server.getPlayer(uuid)?.let { BukkitKhsPlayer(plugin, it) }
    }

    override fun getPlayer(name: String): KhsPlayer? {
        return plugin.server.getPlayer(name)?.let { BukkitKhsPlayer(plugin, it) }
    }

    override fun wrapPlayer(inner: Any?): KhsPlayer? {
        val player = inner as? BukkitPlayer ?: return null
        return BukkitKhsPlayer(plugin, player)
    }

    override fun getWorld(worldName: String): KhsWorld? {
        return plugin.server.getWorld(worldName)?.let { BukkitKhsWorld(this, it) }
    }

    override fun getWorldLoader(worldName: String): KhsWorld.Loader {
        return BukkitKhsWorldLoader(plugin, worldName)
    }

    override fun createWorld(worldName: String, type: KhsWorld.Type): KhsWorld? {
        val worldType = if (type == KhsWorld.Type.FLAT) WorldType.FLAT else WorldType.NORMAL
        val env =
            when (type) {
                KhsWorld.Type.NETHER -> BukkitWorld.Environment.NETHER
                KhsWorld.Type.END -> BukkitWorld.Environment.THE_END
                else -> BukkitWorld.Environment.NORMAL
            }
        val creator = WorldCreator(worldName)
        creator.type(worldType)
        creator.environment(env)
        plugin.server.createWorld(creator)
        val world = plugin.server.getWorld(worldName) ?: return null
        world.save()
        return BukkitKhsWorld(plugin.shim, world)
    }

    override fun createInventory(title: String, size: UInt): KhsInventory {
        val inv = plugin.server.createInventory(null, size.toInt(), title)
        return BukkitKhsInventory(this, inv, title)
    }

    override fun getBoard(name: String): KhsBoard? =
        runCatching {
                val board = plugin.server.scoreboardManager?.newScoreboard ?: return null
                return BukkitKhsBoard(this, board)
            }
            .getOrElse { null }

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
