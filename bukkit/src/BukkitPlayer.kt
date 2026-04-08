package cat.freya.khs.bukkit

import cat.freya.khs.game.Board
import cat.freya.khs.world.Inventory
import cat.freya.khs.world.Location
import cat.freya.khs.world.Material
import cat.freya.khs.world.Player
import cat.freya.khs.world.Vector
import com.cryptomorin.xseries.XSound
import com.cryptomorin.xseries.messages.ActionBar
import com.cryptomorin.xseries.messages.Titles
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.util.BlockIterator

class BukkitPlayer(plugin: KhsPlugin, val inner: org.bukkit.entity.Player) :
    BukkitEntity(plugin, inner), Player {
    override val uuid = inner.uniqueId
    override val name = inner.name

    override fun getHealth(): Double {
        return inner.health
    }

    override fun setHealth(health: Double) {
        inner.health = health
    }

    @Suppress("DEPRECATION")
    override fun heal() {
        if (!plugin.shim.supports(9)) {
            // 1.8 doesnt support attributes
            inner.health = inner.maxHealth
            return
        }

        val attributeName = if (plugin.shim.supports(21, 6)) "MAX_HEALTH" else "GENERIC_MAX_HEALTH"
        val attribute = inner.getAttribute(Attribute.valueOf(attributeName))
        inner.health = attribute?.value ?: 20.0
    }

    override fun getHunger(): UInt {
        return inner.foodLevel.toUInt()
    }

    override fun satiate() {
        inner.foodLevel = 20
    }

    override fun knockBack(direction: Vector) {
        val velocity = inner.velocity
        velocity.x = direction.x * 0.4
        velocity.y = 0.4
        velocity.z = direction.z * 0.4
        inner.velocity = velocity
    }

    override fun getAllowedFlight(): Boolean {
        return inner.allowFlight
    }

    override fun setAllowedFlight(allowedFlight: Boolean) {
        inner.allowFlight = allowedFlight
    }

    override fun getFlying(): Boolean {
        return inner.isFlying
    }

    override fun setFlying(flying: Boolean) {
        if (inner.isFlying != flying) {
            inner.fallDistance = 0f
        }

        runCatching { inner.isFlying = flying }
    }

    override fun getInventory(): BukkitPlayerInventory {
        return BukkitPlayerInventory(plugin.shim, inner.inventory, null)
    }

    override fun showInventory(inv: Inventory) {
        val inventory = (inv as? BukkitInventory)?.inner ?: return
        inner.openInventory(inventory)
    }

    override fun closeInventory() {
        inner.closeInventory()
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

    @Suppress("UnstableApiUsage")
    override fun playSound(sound: String, volume: Double, pitch: Double) {
        XSound.REGISTRY.getByName(sound).ifPresent {
            it.play(inner, volume.toFloat(), pitch.toFloat())
        }
    }

    override fun createDisguise(material: Material): BukkitDisguise? {
        val bukkitMaterial = (material as? BukkitMaterial) ?: return null
        return BukkitDisguise(plugin, inner.uniqueId, bukkitMaterial)
    }

    @Suppress("DEPRECATION")
    override fun getAttackDamage(): Double {
        // attributes are 1.9+ only...
        if (!plugin.shim.supports(9)) return 0.0

        val attributeName =
            if (plugin.shim.supports(21)) "ATTACK_DAMAGE" else "GENERIC_ATTACK_DAMAGE"
        val attribute = inner.getAttribute(Attribute.valueOf(attributeName))
        return attribute?.value ?: return 0.0
    }

    override fun getEyePosition(): Location {
        val loc = inner.eyeLocation
        return Location(loc.x, loc.y, loc.z, inner.world.name)
    }

    override fun getEyeDirection(): Vector {
        val vec = inner.eyeLocation.direction
        return Vector(vec.x, vec.y, vec.z)
    }

    private fun modernRayTrace(maxReach: Double): Double? {
        val result =
            inner.world.rayTraceBlocks(inner.eyeLocation, inner.eyeLocation.direction, maxReach)
                ?: return null
        return result.hitPosition.distance(inner.eyeLocation.toVector())
    }

    private fun legacyRayTrace(maxReach: Double): Double? {
        val eye = inner.eyeLocation
        val direction = eye.direction

        val iterator =
            BlockIterator(inner.world, inner.location.toVector(), direction, 0.0, maxReach.toInt())

        var distance = 0.0

        while (iterator.hasNext()) {
            val block = iterator.next()
            distance += 1.0

            if (block.type == org.bukkit.Material.AIR) continue

            for (i in 0 until 10) {
                val d = 0.1 * i
                val point = eye.clone().add(direction.clone().multiply(d))

                if (point.block == block) return distance + d
            }
        }

        return null
    }

    override fun getReach(maxReach: Double): Double? {
        return if (plugin.shim.supports(13, 2)) {
            modernRayTrace(maxReach)
        } else {
            legacyRayTrace(maxReach)
        }
    }

    override fun getGameMode(): Player.GameMode {
        return when (inner.gameMode) {
            org.bukkit.GameMode.SURVIVAL -> Player.GameMode.SURVIVAL
            org.bukkit.GameMode.CREATIVE -> Player.GameMode.CREATIVE
            org.bukkit.GameMode.ADVENTURE -> Player.GameMode.ADVENTURE
            org.bukkit.GameMode.SPECTATOR -> Player.GameMode.SPECTATOR
        }
    }

    override fun setGameMode(gameMode: Player.GameMode) {
        inner.gameMode =
            when (gameMode) {
                Player.GameMode.SURVIVAL -> org.bukkit.GameMode.SURVIVAL
                Player.GameMode.CREATIVE -> org.bukkit.GameMode.CREATIVE
                Player.GameMode.ADVENTURE -> org.bukkit.GameMode.ADVENTURE
                Player.GameMode.SPECTATOR -> org.bukkit.GameMode.SPECTATOR
            }
    }

    override fun hasPermission(permission: String): Boolean {
        return inner.hasPermission(permission)
    }

    override fun getScoreBoard(): BukkitBoard {
        return BukkitBoard(plugin.shim, inner.scoreboard)
    }

    override fun setScoreBoard(board: Board?) {
        val default = plugin.server.scoreboardManager?.mainScoreboard
        val bukkitBoard = (board as? BukkitBoard)?.inner ?: default ?: return
        inner.scoreboard = bukkitBoard
    }

    override fun taunt() {
        val world = getWorld() ?: return
        val location = getLocation()
        val bukkitLocation = org.bukkit.Location(world.inner, location.x, location.y, location.z)

        // spawn firework
        val fireworkTypeName = if (plugin.shim.supports(13)) "FIREWORK_ROCKET" else "FIREWORK"
        val fireworkType = EntityType.valueOf(fireworkTypeName)
        val firework = world.inner.spawnEntity(bukkitLocation, fireworkType) as Firework
        firework.velocity = org.bukkit.util.Vector(0, 1, 0)

        // make it pretty
        val meta = firework.fireworkMeta
        meta.power = 4
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
        firework.fireworkMeta = meta
    }

    override fun sendPacket(packet: PacketWrapper<*>) {
        PacketEvents.getAPI().playerManager.sendPacket(inner, packet)
    }
}
