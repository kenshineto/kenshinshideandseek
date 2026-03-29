package cat.freya.khs.bukkit

import cat.freya.khs.KhsShim
import cat.freya.khs.Logger
import cat.freya.khs.config.EffectConfig
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.Board as KhsBoard
import cat.freya.khs.player.Inventory as KhsInventory
import cat.freya.khs.player.Player as KhsPlayer
import cat.freya.khs.world.Effect as KhsEffect
import cat.freya.khs.world.Item as KhsItem
import cat.freya.khs.world.World as KhsWorld
import com.cryptomorin.xseries.XMaterial
import java.io.File
import java.io.InputStream
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.World as BukkitWorld
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class BukkitLogger(val plugin: KhsPlugin) : Logger {
    override fun info(message: String) = plugin.logger.info(message)

    override fun warning(message: String) = plugin.logger.warning(message)

    override fun error(message: String) = plugin.logger.severe(message)
}

class BukkitKhsShim(val plugin: KhsPlugin) : KhsShim {
    override val pluginVersion: String
    override val mcVersion: List<UInt>
    override val platform: String = "Bukkit"

    init {
        // parse mc version
        mcVersion =
            Regex("""MC:\s*([\d.]+)""")
                .find(plugin.server.version)
                ?.groupValues
                ?.get(1)
                ?.split('.')
                ?.asSequence()
                ?.mapNotNull { it.toUIntOrNull() }
                ?.let { seq -> if (seq.firstOrNull() == 1u) seq.drop(1) else seq }
                ?.toList() ?: emptyList()

        pluginVersion = plugin.description.version
    }

    override val logger = BukkitLogger(plugin)

    override val players: List<KhsPlayer>
        get() = plugin.server.onlinePlayers.map { BukkitKhsPlayer(this, it) }

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
        get() = Material.values().map { it.toString().uppercase() }

    override val sqliteDatabasePath: String
        get() {
            val legacy = File(plugin.dataFolder.path, "database.db")
            if (legacy.exists()) return legacy.path

            return File(plugin.dataFolder.path, "khs.db").path
        }

    override fun readConfigFile(fileName: String): InputStream? {
        val dir = plugin.dataFolder
        if (!dir.exists()) {
            dir.mkdirs() || error("Failed to make plugin config directory")
        }
        val file = File(dir, fileName)
        return if (file.exists()) file.inputStream() else null
    }

    override fun writeConfigFile(fileName: String, content: String) {
        val dir = plugin.dataFolder
        if (!dir.exists()) {
            dir.mkdirs() || error("Failed to make plugin config directory")
        }
        val file = File(dir, fileName)
        file.writeText(content)
    }

    override fun parseMaterial(materialName: String): String? {
        return XMaterial.matchXMaterial(materialName).getOrNull()?.get()?.toString()
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
        return plugin.server.getPlayer(uuid)?.let { BukkitKhsPlayer(this, it) }
    }

    override fun getPlayer(name: String): KhsPlayer? {
        return plugin.server.getPlayer(name)?.let { BukkitKhsPlayer(this, it) }
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
        var world = plugin.server.getWorld(worldName) ?: return null
        world.save()
        return BukkitKhsWorld(plugin.shim, world)
    }

    override fun createInventory(title: String, size: UInt): KhsInventory? {
        val inv = plugin.server.createInventory(null, size.toInt(), title)
        return BukkitKhsInventory(this, inv, title)
    }

    override fun getBoard(name: String): KhsBoard? {
        val board = plugin.server.scoreboardManager?.getNewScoreboard() ?: return null
        return BukkitKhsBoard(this, board)
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

fun formatText(message: String): String {
    return ChatColor.translateAlternateColorCodes('&', message)
}
