package cat.freya.khs.event

import cat.freya.khs.Khs
import cat.freya.khs.disguise.Disguise
import cat.freya.khs.world.Player
import cat.freya.khs.world.Vector
import cat.freya.khs.world.VectorAABB
import java.util.UUID

private const val BLOCKHUNT_MAX_REACH: Double = 5.0

/**
 * Don't let people spam attack disguised players. Since we are emulating the attacks ourselves, we
 * have to emulate this too
 */
private val debounce: MutableSet<UUID> = mutableSetOf()

data class SwingEvent(val plugin: Khs, val player: Player) : Event()

private fun handleAttack(plugin: Khs, disguise: Disguise, attacker: Player) {
    val player = disguise.player ?: return
    if (player.uuid == attacker.uuid) return

    val debounceUUID = player.uuid
    if (debounce.contains(debounceUUID)) return

    // trigger an attack event
    val damage = attacker.getAttackDamage()
    val khsEvent = DamageEvent(plugin, player, attacker, damage)
    onDamage(khsEvent)

    // emulate the attacker attacking the
    // player
    if (!khsEvent.cancelled) {
        val direction = attacker.getEyeDirection().normalize()
        disguise.shouldBeSolid = false
        player.damage(damage)
        player.playSound("ENTITY_PLAYER_HURT", 1.0, 1.0)
        player.knockBack(direction)
    }

    // set and soon turn off debounce
    debounce.add(debounceUUID)
    plugin.shim.scheduleEvent(10UL) { debounce.remove(debounceUUID) }
}

fun onSwing(event: SwingEvent) {
    val (plugin, player) = event

    // ray cast to detect for
    // disguises

    val reach = player.getReach(BLOCKHUNT_MAX_REACH) ?: BLOCKHUNT_MAX_REACH
    val eye = player.getEyePosition().toPosition().toVector()
    val direction = player.getEyeDirection().normalize()

    val disguise =
        plugin.disguiser
            .mapDisguises { disguise ->
                val loc = disguise.getCurrentBlockLocation() ?: return@mapDisguises null

                // make aabb
                val vector = loc.toPosition().toVector()
                val min = vector.subtract(Vector(0.5, 0.0, 0.5))
                val max = vector.add(Vector(0.5, 1.0, 0.5))
                val aabb = VectorAABB(min, max)

                // ray cast to aabb
                val distance = aabb.rayIntersects(eye, direction) ?: return@mapDisguises null
                disguise to distance
            }
            .filter { (_, distance) -> distance <= reach }
            .minByOrNull { it.second }
            ?.first ?: return

    handleAttack(plugin, disguise, player)
}
