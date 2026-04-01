package cat.freya.khs.disguise

import cat.freya.khs.Khs
import cat.freya.khs.packet.BlockChangePacket
import cat.freya.khs.packet.EntityTeleportPacket
import cat.freya.khs.player.Player
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Location
import cat.freya.khs.world.Material
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

    init {
        val player = player ?: error("invalid uuid")
        player.setCollides(false)
    }

    // the floating falling sand/display entity
    var block: Entity? = null
        private set

    abstract fun createBlock(location: Location): Entity?

    private fun destroyBlock() {
        block?.destroy()
        block = null
    }

    private fun respawnBlock() {
        val loc = player?.location?.clone() ?: return

        // keep it out of the way till we
        // teleport it
        loc.y += 1000.0

        destroyBlock()
        block = createBlock(loc)
    }

    var hitBox: Entity? = null
        private set

    abstract fun createHitBox(location: Location): Entity?

    private fun destroyHitBox() {
        hitBox?.destroy()
        hitBox = null
    }

    private fun respawnHitBox() {
        val loc = player?.location?.clone() ?: return

        // keep it out of the way till we
        // teleport it
        loc.y += 1000.0

        destroyHitBox()
        hitBox = createHitBox(loc)
        hitBox?.setInvisible(true)
        hitBox?.setCollides(false)
    }

    fun update() {
        val player = player ?: return

        // make sure the block exists
        if (block?.isAlive != true) respawnBlock()

        if (shouldBeSolid) {
            if (!isSolid) {
                isSolid = true
                solidifiedPosition = player.location.clone()
                respawnHitBox()
            }
            sendBlockUpdate(material)
        } else if (isSolid) {
            isSolid = false
            destroyHitBox()
            respawnBlock()
            sendBlockUpdate(null)
        }

        updateVisibility()
        teleportEntity(hitBox, true)
        teleportEntity(block, isSolid)

        // do this here is it can be
        // cleared by the core game logic
        player.setInvisible(true)
    }

    // handle solidifying
    fun startSolidifying(last: Position) {
        if (isSolid || hasSolidifyingTask) return
        hasSolidifyingTask = solidifyUpdate(last, DISGUISE_SOLIDIFY_TIME)
    }

    private fun solidifyUpdate(last: Position, time: UInt): Boolean {
        val player = player ?: return false
        val current = player.location.position

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
        destroyHitBox()

        val player = player ?: return
        player.setCollides(true)
        player.setInvisible(false)
        if (isSolid) sendBlockUpdate(null)
    }

    private fun teleportEntity(entity: Entity?, center: Boolean) {
        if (entity == null) return

        val player = player ?: return
        val loc = player.location.clone()
        if (center) {
            loc.x = round(loc.x + 0.5) - 0.5
            loc.y = round(loc.y)
            loc.z = round(loc.z + 0.5) - 0.5
        }

        val packet = EntityTeleportPacket(entity, loc)
        plugin.shim.players.forEach { packet.send(it) }
    }

    private fun sendBlockUpdate(wantedMaterial: Material?) {
        val location = solidifiedPosition ?: return
        val material = wantedMaterial ?: plugin.shim.parseMaterial("AIR") ?: return
        val packet = BlockChangePacket(location, material)
        plugin.shim.players.forEach {
            if (it.uuid == uuid) return@forEach
            packet.send(it)
        }
    }

    private fun updateVisibility() {
        val block = block ?: return
        val show = !isSolid
        plugin.shim.players.forEach { target ->
            if (target.uuid == uuid) return@forEach

            if (show) {
                plugin.entityHider.showEntity(target, block)
            } else {
                plugin.entityHider.hideEntity(target, block)
            }
        }
    }
}
