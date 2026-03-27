package cat.freya.khs.bukkit

import cat.freya.khs.Khs
import cat.freya.khs.bukkit.disguise.Disguiser
import cat.freya.khs.bukkit.disguise.EntityHider
import cat.freya.khs.bukkit.event.*
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player as BukkitPlayer
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class KhsPlugin : JavaPlugin() {
    val shim: BukkitKhsShim = BukkitKhsShim(this)
    val khs: Khs = Khs(shim)

    // for blockhunt
    val disguiser: Disguiser = Disguiser(this)
    val entityHider: EntityHider = EntityHider()

    override fun onEnable() {
        khs.init()

        if (!this.isEnabled()) return

        // make sure onTick is run
        object : BukkitRunnable() {
                override fun run() {
                    khs.onTick()
                    disguiser.update()
                }
            }
            .runTaskTimer(this, 0, 1)

        // register bungee cord
        server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")

        registerListeners()
    }

    override fun onDisable() {
        khs.cleanup()
        disguiser.cleanup()
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
        PacketListener(this)
        RespawnListener(this)
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
        val player = sender as? BukkitPlayer ?: return false
        val khsPlayer = BukkitKhsPlayer(shim, player)
        khs.commandGroup.handleCommand(khsPlayer, args.toList())
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<String>,
    ): List<String> {
        val player = sender as? BukkitPlayer ?: return listOf()
        val khsPlayer = BukkitKhsPlayer(shim, player)
        return khs.commandGroup.handleTabComplete(khsPlayer, args.toList())
    }
}
