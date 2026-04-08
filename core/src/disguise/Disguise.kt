package cat.freya.khs.disguise

import cat.freya.khs.Khs
import cat.freya.khs.packet.BlockChangePacket
import cat.freya.khs.packet.EntityTeleportPacket
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Location
import cat.freya.khs.world.Material
import cat.freya.khs.world.Player
import java.util.UUID
import kotlin.math.round

/** How long it takes to solidify */
const val DISGUISE_SOLIDIFY_TIME = 3u

/** How much a player can move and won't become unsolid from moving */
const val DISGUISE_MOVE_THRESHOLD = 0.1

abstract class Disguise(val plugin: Khs, val uuid: UUID, val material: Material) {
    // returns the player associated with this disguise
    val player: Player?
        get() = plugin.shim.getPlayer(uuid)

    // if the disguise is allowed to solidify
    var shouldBeSolid: Boolean = false

    // if the disguise is currently solidified
    private var isSolid: Boolean = false

    // last known player position
    private var lastPlayerPosition: Location? = null

    // how many ticks has this disguise been solidifying
    private var solidifyTimer: UInt = 0u

    // where we are currently solidified
    private var solidifiedPosition: Location? = null

    // the floating falling sand/display entity
    private var block: Entity? = null

    init {
        val player = player ?: error("invalid uuid")
        player.setCollides(false)
    }

    abstract fun createBlock(location: Location): Entity?

    private fun destroyBlock() {
        block?.destroy()
        block = null
    }

    private fun respawnBlock() {
        val loc = player?.getLocation()?.clone() ?: return

        // keep it out of the way till we
        // teleport it
        loc.y += 1000.0

        destroyBlock()
        block = createBlock(loc)
    }

    fun update() {
        val player = player ?: return

        // make sure the block exists
        if (block?.isAlive() != true) respawnBlock()

        if (shouldBeSolid) {
            if (!isSolid) {
                isSolid = true
                solidifiedPosition = player.getLocation().clone()
            }
            sendBlockUpdate(material)
        } else if (isSolid) {
            isSolid = false
            solidifyTimer = 0u
            respawnBlock()
            sendBlockUpdate(null)
        }

        solidifyUpdate()
        updateVisibility()
        teleportBlock()
    }

    fun destroy() {
        destroyBlock()

        // reset player physics
        val player = player ?: return
        player.setCollides(true)
        plugin.entityHider.showEntity(player)

        // make sure the client side blocks
        // are removed
        if (isSolid) {
            sendBlockUpdate(null)
        }
    }

    fun getCurrentBlockLocation(): Location? {
        val player = player ?: return null
        val loc = player.getLocation().clone()
        if (isSolid) {
            // center the block
            loc.x = round(loc.x + 0.5) - 0.5
            loc.y = round(loc.y)
            loc.z = round(loc.z + 0.5) - 0.5
        }

        return loc
    }

    private fun solidifyUpdate() {
        val player = player ?: return
        val current = player.getLocation()
        val last = lastPlayerPosition ?: current

        val difference = current.distance(last)
        if (difference == null || difference >= DISGUISE_MOVE_THRESHOLD) {
            // player moved in a substantial way
            solidifyTimer = 0u
            shouldBeSolid = false
            lastPlayerPosition = current
            return
        }

        if (isSolid) return

        // we have been staying still for [solidifyTimer] ticks!
        val isSecond = (solidifyTimer % 20u) == 0u
        val seconds = solidifyTimer / 20u
        solidifyTimer++
        lastPlayerPosition = last

        if (seconds > DISGUISE_SOLIDIFY_TIME) {
            // we have solidified
            player.actionBar("")
            shouldBeSolid = true
            return
        }

        if (isSecond && seconds >= 1u) {
            val secondsLeft = DISGUISE_SOLIDIFY_TIME - (seconds - 1u)
            player.actionBar("▪".repeat(secondsLeft.toInt()))
            player.playSound("BLOCK_NOTE_BLOCK_PLING", 0.5, 1.0)
        }
    }

    private fun teleportBlock() {
        val block = block ?: return
        val loc = getCurrentBlockLocation() ?: return
        val packet = EntityTeleportPacket(block, loc)
        plugin.shim.getPlayers().forEach { packet.send(it) }
    }

    private fun sendBlockUpdate(wantedMaterial: Material?) {
        val location = solidifiedPosition ?: return
        val material = wantedMaterial ?: plugin.shim.parseMaterial("AIR") ?: return
        val packet = BlockChangePacket(location, material)
        plugin.shim.getPlayers().forEach {
            if (it.uuid == uuid) return@forEach
            packet.send(it)
        }
    }

    private fun updateVisibility() {
        val block = block ?: return
        if (isSolid) {
            plugin.entityHider.hideEntity(block, uuid)
        } else {
            plugin.entityHider.showEntity(block)
        }

        val player = player ?: return
        plugin.entityHider.hideEntity(player, uuid)
    }
}
