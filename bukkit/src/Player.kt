package cat.freya.khs.bukkit

import cat.freya.khs.bukkit.disguise.Disguise
import cat.freya.khs.disguise.Disguise as KhsDisguise
import cat.freya.khs.player.Inventory as KhsInventory
import cat.freya.khs.player.Player as KhsPlayer
import cat.freya.khs.player.Player.GameMode as KhsGameMode
import cat.freya.khs.player.PlayerInventory as KhsPlayerInventory
import cat.freya.khs.world.Effect
import cat.freya.khs.world.Location
import cat.freya.khs.world.Material as KhsMaterial
import cat.freya.khs.world.Position
import cat.freya.khs.world.World as KhsWorld
import com.cryptomorin.xseries.XSound
import com.cryptomorin.xseries.messages.ActionBar
import com.cryptomorin.xseries.messages.Titles
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import com.google.common.io.ByteStreams
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.GameMode as BukkitGameMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player as BukkitPlayer
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

class BukkitKhsPlayer(val plugin: KhsPlugin, val inner: BukkitPlayer) : KhsPlayer {
    override val uuid = inner.uniqueId
    override val name = inner.name

    private val shim = plugin.shim
    private val innerEntity = BukkitKhsEntity(plugin, inner)

    override val location: Location
        get() {
            val loc = inner.location
            return Location(loc.x, loc.y, loc.z, inner.world.name.intern())
        }

    override val world: KhsWorld?
        get() = shim.getWorld(location.worldName)

    override var health: Double
        get() = inner.health
        set(v: Double) {
            inner.health = v
        }

    override var hunger: UInt
        get() = inner.foodLevel.toUInt()
        set(v: UInt) {
            inner.foodLevel = v.toInt()
        }

    override fun heal() {
        if (shim.supports(9)) {
            val attribName = if (shim.supports(21, 6)) "MAX_HEALTH" else "GENERIC_MAX_HEALTH"
            var attrib = inner.getAttribute(Attribute.valueOf(attribName))
            health = attrib?.value ?: 20.0
        } else {
            @Suppress("DEPRECATION")
            health = inner.maxHealth
        }
    }

    override var allowFlight
        get() = inner.allowFlight
        set(v: Boolean) {
            inner.allowFlight = v
        }

    override var flying
        get() = inner.isFlying
        set(flying: Boolean) {
            if (this.flying != flying) inner.setFallDistance(0f)
            runCatching { inner.setFlying(flying) }
        }

    override fun teleport(position: Position) {
        val loc = Location(position.x, position.y, position.z, inner.world.name)
        teleport(loc)
    }

    override fun teleport(location: Location) {
        var world = shim.plugin.server.getWorld(location.worldName)
        if (world == null) {
            // attempt to load the world
            val loader = shim.getWorldLoader(location.worldName)
            loader.load()
            world = shim.plugin.server.getWorld(location.worldName)
        }
        val x = location.x
        val y = location.y
        val z = location.z
        val pos = org.bukkit.Location(world, x, y, z)

        // sanity check
        if (world == null) {
            shim.logger.warning("Could not teleport $name to $x,$y,$z in ${location.worldName}")
            return
        }

        inner.teleport(pos)
    }

    override fun sendToServer(server: String) {
        val out = ByteStreams.newDataOutput()
        out.writeUTF("Connect")
        out.writeUTF(shim.plugin.khs.config.leaveServer)
        inner.sendPluginMessage(shim.plugin, "BungeeCord", out.toByteArray())
    }

    override val inventory: KhsPlayerInventory
        get() = BukkitKhsPlayerInventory(shim, inner.inventory, null)

    override fun showInventory(inv: KhsInventory) {
        inner.openInventory((inv as BukkitKhsInventory).inner)
    }

    override fun closeInventory() {
        inner.closeInventory()
    }

    override fun clearEffects() {
        for (effect in inner.activePotionEffects) inner.removePotionEffect(effect.type)
    }

    override fun giveEffect(effect: Effect) {
        inner.addPotionEffect((effect as BukkitKhsEffect).inner)
    }

    override fun setSpeed(amplifier: UInt) {
        inner.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 1000000, 5, false, false))
    }

    override fun setHidden(target: KhsPlayer, hidden: Boolean) {
        var other = (target as BukkitKhsPlayer).inner
        if (shim.supports(12, 2)) {
            if (hidden) inner.hidePlayer(shim.plugin, other)
            else inner.showPlayer(shim.plugin, other)
        } else {
            @Suppress("DEPRECATION")
            if (hidden) inner.hidePlayer(other) else inner.showPlayer(other)
        }
    }

    override fun message(message: String) {
        inner.sendMessage(formatText(message))
    }

    override fun actionBar(message: String) {
        ActionBar.clearActionBar(inner)
        ActionBar.sendActionBar(inner, formatText(message))
    }

    override fun title(title: String, subTitle: String) {
        Titles.clearTitle(inner)
        Titles.sendTitle(inner, 10, 40, 10, formatText(title), formatText(subTitle))
    }

    override fun playSound(sound: String, volume: Double, pitch: Double) {
        XSound.REGISTRY.getByName(sound).ifPresent {
            it.play(inner, volume.toFloat(), pitch.toFloat())
        }
    }

    override fun createDisguise(material: KhsMaterial): KhsDisguise? =
        Disguise(shim.plugin, inner.uniqueId, material)

    override fun hasPermission(permission: String): Boolean {
        return inner.hasPermission(permission)
    }

    override var gameMode: KhsGameMode
        get() =
            when (inner.gameMode) {
                BukkitGameMode.CREATIVE -> KhsGameMode.CREATIVE
                BukkitGameMode.SURVIVAL -> KhsGameMode.SURVIVAL
                BukkitGameMode.ADVENTURE -> KhsGameMode.ADVENTURE
                BukkitGameMode.SPECTATOR -> KhsGameMode.SPECTATOR
            }
        set(gameMode: KhsGameMode) =
            inner.setGameMode(
                when (gameMode) {
                    KhsGameMode.CREATIVE -> BukkitGameMode.CREATIVE
                    KhsGameMode.SURVIVAL -> BukkitGameMode.SURVIVAL
                    KhsGameMode.ADVENTURE -> BukkitGameMode.ADVENTURE
                    KhsGameMode.SPECTATOR -> BukkitGameMode.SPECTATOR
                }
            )

    override fun hideBoards() {
        val manager = shim.plugin.server.scoreboardManager ?: return
        inner.setScoreboard(manager.mainScoreboard)
    }

    override fun taunt() {
        val world = this.world?.let { it as BukkitKhsWorld } ?: return
        val loc = org.bukkit.Location(world.inner, location.x, location.y, location.z)

        // spawn firework
        val fwMatName = if (shim.supports(13)) "FIREWORK_ROCKET" else "FIREWORK"
        val fwMat = EntityType.valueOf(fwMatName)
        val fw = world.inner.spawnEntity(loc, fwMat) as Firework
        fw.setVelocity(Vector(0, 1, 0))

        // make it pretty
        val meta = fw.fireworkMeta
        meta.setPower(4)
        meta.addEffect(
            FireworkEffect.builder()
                .withColor(Color.BLUE)
                .withColor(Color.RED)
                .withColor(Color.YELLOW)
                .with(FireworkEffect.Type.STAR)
                .with(FireworkEffect.Type.BALL)
                .with(FireworkEffect.Type.BALL_LARGE)
                .flicker(true)
                .withTrail()
                .build()
        )
        fw.fireworkMeta = meta
    }

    // entity wrappers

    override val entityId: Int = innerEntity.entityId

    override val isAlive: Boolean
        get() = innerEntity.isAlive

    override fun setCollides(collides: Boolean) = innerEntity.setCollides(collides)

    override fun setInvisible(invisible: Boolean) = innerEntity.setInvisible(invisible)

    // uh, how about we do nothin
    override fun destroy() {}

    override fun sendPacket(packet: PacketWrapper<*>) {
        PacketEvents.getAPI().playerManager.sendPacket(inner, packet)
    }
}
