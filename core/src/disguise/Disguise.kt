package cat.freya.khs.disguise

import cat.freya.khs.Khs
import cat.freya.khs.packet.BlockChangePacket
import cat.freya.khs.packet.EntityTeleportPacket
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Location
import cat.freya.khs.world.Material
import cat.freya.khs.world.Player
import cat.freya.khs.world.Position
import java.util.UUID
import kotlin.math.round

// in seconds
const val DISGUISE_SOLIDIFY_TIME = 3u

abstract class Disguise(val plugin: Khs, val uuid: UUID, val material: Material) {
    // returns the player associated with this disguise
    val player: Player?
        get() = plugin.shim.getPlayer(uuid)

    // if the disguise is allowed to solidify
    var shouldBeSolid: Boolean = false

    // if the disguise is currently solidified
    private var isSolid: Boolean = false

    // if this disguise is currently solidifying
    private var hasSolidifyingTask = false

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
            respawnBlock()
            sendBlockUpdate(null)
        }

        updateVisibility()
        teleportBlock()
    }

    // handle solidifying
    fun startSolidifying(last: Position) {
        if (isSolid || hasSolidifyingTask) return
        hasSolidifyingTask = solidifyUpdate(last, DISGUISE_SOLIDIFY_TIME)
    }

    private fun solidifyUpdate(last: Position, time: UInt): Boolean {
        val player = player ?: return false
        val current = player.getLocation().toPosition()

        if (last.distance(current) > 0.1) return false

        // we have solidified!
        if (time == 0u) {
            player.actionBar("")
            shouldBeSolid = true
            return false
        }

        // still waiting
        player.actionBar("▪".repeat(time.toInt()))
        player.playSound("BLOCK_NOTE_BLOCK_PLING", 0.5, 1.0)

        // schedule next update
        plugin.shim.scheduleEvent(20UL) { hasSolidifyingTask = solidifyUpdate(last, time - 1u) }
        return true
    }

    fun destroy() {
        destroyBlock()

        // reset player physics
        val player = player ?: return
        player.setCollides(true)
        plugin.entityHider.showEntity(player)

        // make sure the client side block's
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
