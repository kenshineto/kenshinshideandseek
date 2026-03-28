package cat.freya.khs.bukkit.disguise

import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.bukkit.packet.BlockChangePacket
import cat.freya.khs.bukkit.packet.EntityTeleportPacket
import com.cryptomorin.xseries.XSound
import com.cryptomorin.xseries.messages.ActionBar
import kotlin.math.round
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.AbstractHorse
import org.bukkit.entity.Entity as BukkitEntity
import org.bukkit.entity.EntityType
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player as BukkitPlayer
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team

private fun makeInvisible(entity: LivingEntity) {
    if (entity.hasPotionEffect(PotionEffectType.INVISIBILITY)) return

    entity.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 1_000_000, 0, false, false))
}

private fun getCollidesTeam(plugin: KhsPlugin): Team? {
    val KHS_DISGUISE_TEAM_NAME = "KHS_disguised"
    val scoreboard = plugin.server.scoreboardManager?.mainScoreboard ?: return null
    val team =
        scoreboard.getTeam(KHS_DISGUISE_TEAM_NAME)
            ?: scoreboard.registerNewTeam(KHS_DISGUISE_TEAM_NAME)
    team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
    team.setCanSeeFriendlyInvisibles(false)
    return team
}

private fun setCollides(plugin: KhsPlugin, player: BukkitPlayer, collides: Boolean) {
    if (plugin.shim.supports(9)) {
        val team = getCollidesTeam(plugin)
        if (collides) team?.removeEntry(player.name) else team?.addEntry(player.name)
    } else {
        val method = player.spigot().javaClass.getMethod("setCollidesWithEntities")
        method.invoke(player, collides)
    }
}

class Disguise(val plugin: KhsPlugin, val player: BukkitPlayer, val material: Material) {
    var block: FallingBlock? = null
    var blockLocation: Location? = null
    var hitBox: AbstractHorse? = null
    var timer: UInt = 0u

    @Volatile var shouldBeSolid: Boolean = false
    @Volatile var isSolid: Boolean = false
    @Volatile var hasSolidifyingTask: Boolean = false

    init {
        // make sure the player does not collide
        setCollides(plugin, player, false)
    }

    val entityId: Int?
        get() = block?.entityId

    val hitBoxId: Int?
        get() = hitBox?.entityId

    fun update() {
        if (block?.isDead() != false) {
            block?.remove()
            respawnFallingBlock()
        }

        if (shouldBeSolid) {
            if (!isSolid) {
                isSolid = true
                blockLocation = player.location.block.location
                respawnHitbox()
            }
            sendBlockUpdate(blockLocation, material)
            block?.setTicksLived(1)
        } else if (isSolid) {
            isSolid = false
            removeHitbox()
            removeFallingBlock()
            respawnFallingBlock()
            sendBlockUpdate(blockLocation, Material.AIR)
        }

        updateVisiblity()
        teleportEntity(hitBox, true)
        teleportEntity(block, isSolid)

        // do this here is it can be
        // cleared by the core game logic
        makeInvisible(player)
    }

    fun remove() {
        block?.remove()
        removeHitbox()
        setCollides(plugin, player, true)
        player.removePotionEffect(PotionEffectType.INVISIBILITY)
        if (isSolid) sendBlockUpdate(blockLocation, Material.AIR)
    }

    fun removeFallingBlock() {
        block?.remove()
        block = null
    }

    fun respawnFallingBlock() {
        val world = player.location.world ?: return
        val loc = player.location.add(0.0, 1000.0, 0.0)

        val block: FallingBlock? =
            runCatching { world.spawnFallingBlock(loc, material, 0x0) }.getOrElse { null }
        if (block == null) return

        if (plugin.shim.supports(10)) block.setGravity(false)

        block.setDropItem(false)
        block.setInvulnerable(true)

        this.block = block
    }

    fun respawnHitbox() {
        val world = player.location.world ?: return

        // we only want the hitbox to be at our postion
        // when we are solidified
        val loc = player.location.add(0.0, 1000.0, 0.0)

        val hitBox: AbstractHorse? =
            if (plugin.shim.supports(11)) {
                world.spawnEntity(loc, EntityType.SKELETON_HORSE) as AbstractHorse
            } else {
                world.spawnEntity(loc, EntityType.HORSE) as AbstractHorse
            }

        if (hitBox == null) return

        if (plugin.shim.supports(10)) hitBox.setGravity(false)

        val id = hitBox.uniqueId.toString()
        if (plugin.shim.supports(9)) getCollidesTeam(plugin)?.addEntry(id)

        hitBox.setAI(false)
        hitBox.setInvulnerable(true)
        hitBox.setCanPickupItems(false)
        hitBox.setCollidable(false)
        makeInvisible(hitBox)

        this.hitBox = hitBox
    }

    fun removeHitbox() {
        val hb = hitBox ?: return
        val id = hb.uniqueId.toString()

        hb.remove()
        if (plugin.shim.supports(9)) getCollidesTeam(plugin)?.removeEntry(id)

        hitBox == null
    }

    fun teleportEntity(entity: BukkitEntity?, center: Boolean) {
        if (entity == null) return

        val loc = player.location.clone()
        if (center) {
            loc.x = round(loc.x + 0.5) - 0.5
            loc.y = round(loc.y)
            loc.z = round(loc.z + 0.5) - 0.5
        }

        val packet = EntityTeleportPacket(entity, loc)
        plugin.server.onlinePlayers.forEach { packet.send(it) }
    }

    fun sendBlockUpdate(location: Location?, material: Material) {
        if (location == null) return

        val packet = BlockChangePacket(location, material)
        plugin.server.onlinePlayers.forEach {
            if (it.uniqueId == player.uniqueId) return@forEach
            packet.send(it)
        }
    }

    fun updateVisiblity() {
        val block = block ?: return
        val show = !isSolid
        plugin.server.onlinePlayers.forEach { target ->
            if (target.uniqueId == player.uniqueId) return@forEach

            if (show) {
                plugin.entityHider.showEntity(target, block)
            } else {
                plugin.entityHider.hideEntity(target, block)
            }
        }
    }

    fun startSolidifying(lastLocation: Location) {
        if (isSolid || hasSolidifyingTask) return
        hasSolidifyingTask = true
        solidifyUpdate(lastLocation, 3u)
    }

    fun solidifyUpdate(lastLocation: Location, time: UInt) {
        val location = player.location

        if ((lastLocation.world != location.world) || (lastLocation.distance(location) > 0.1)) {
            hasSolidifyingTask = false
            return
        }

        // we have solidified!
        if (time == 0u) {
            ActionBar.clearActionBar(player)
            shouldBeSolid = true
            hasSolidifyingTask = false
            return
        }

        // still waiting
        ActionBar.sendActionBar(player, "▪".repeat(time.toInt()))
        XSound.BLOCK_NOTE_BLOCK_PLING.play(player, 1f, 1f)

        // schedule next update
        plugin.server.scheduler.scheduleSyncDelayedTask(
            plugin,
            { solidifyUpdate(lastLocation, time - 1u) },
            20,
        )
    }
}
