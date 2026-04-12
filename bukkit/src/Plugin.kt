package cat.freya.khs.bukkit

import cat.freya.khs.Khs
import cat.freya.khs.PlaceholderRequest
import cat.freya.khs.bukkit.event.*
import cat.freya.khs.handlePlaceholder
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class KhsPlugin : JavaPlugin() {
    val shim: BukkitKhsShim = BukkitKhsShim(this)
    val khs: Khs = Khs(shim)

    private var onTickTask: BukkitTask? = null

    override fun onEnable() {
        khs.init()

        // khs.init() may disable us
        if (!isEnabled) return

        // make sure onTick is run
        onTickTask =
            object : BukkitRunnable() {
                override fun run() {
                    onTick()
                }
            }.runTaskTimer(this, 0, 1)

        // register bungee cord
        server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")

        registerListeners()
        registerPAPI()
    }

    override fun onDisable() {
        onTickTask?.cancel()
        khs.cleanup()
    }

    private fun onTick() {
        if (!isEnabled) return
        khs.onTick()
    }

    private fun registerListeners() {
        BreakListener(this)
        ChatListener(this)
        CommandListener(this)
        DamageListener(this)
        InteractListener(this)
        InventoryListener(this)
        JoinLeaveListener(this)
        MovementListener(this)
        PlayerListener(this)
        RespawnListener(this)
    }

    private fun registerPAPI() {
        if (server.pluginManager.getPlugin("PlaceholderAPI") == null) return

        shim.logger.info("Registering PlaceholderAPI expansion...")

        val me = this
        object : PlaceholderExpansion() {
            override fun getIdentifier() = "hs"

            override fun getAuthor() = "KenshinEto"

            override fun getVersion() = me.description.version

            override fun persist() = true

            override fun onRequest(player: OfflinePlayer?, params: String): String? {
                val uuid = player?.uniqueId ?: return null
                val req = PlaceholderRequest(me.khs, uuid, params)
                return handlePlaceholder(req)
            }
        }.register()
    }

    fun scheduleTask(fn: () -> Unit) {
        if (!isEnabled) return
        server.scheduler.runTask(this, fn)
    }

    override fun onCommand(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        val player = sender as? org.bukkit.entity.Player ?: return false
        val khsPlayer = BukkitPlayer(this, player)
        khs.commandGroup.handleCommand(khsPlayer, args.toList())
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<String>,
    ): List<String> {
        val player = sender as? org.bukkit.entity.Player ?: return listOf()
        val khsPlayer = BukkitPlayer(this, player)
        return khs.commandGroup.handleTabComplete(khsPlayer, args.toList())
    }
}
