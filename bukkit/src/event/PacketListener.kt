package cat.freya.khs.bukkit.event

import cat.freya.khs.bukkit.BukkitKhsPlayer
import cat.freya.khs.bukkit.KhsPlugin
import cat.freya.khs.bukkit.disguise.Disguise
import cat.freya.khs.event.DamageEvent
import cat.freya.khs.event.onDamage
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListener as PacketListenerPE
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client.INTERACT_ENTITY
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server.*
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity
import com.github.retrooper.packetevents.wrapper.play.server.*
import java.util.UUID
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player as BukkitPlayer

class PacketListener(val plugin: KhsPlugin) : PacketListenerPE {
    private val debounce = mutableSetOf<UUID>()

    init {
        PacketEvents.getAPI().eventManager.registerListener(this, PacketListenerPriority.NORMAL)
    }

    // intercept entity-related packets of entities that
    // are supposed to be hidden
    override fun onPacketSend(event: PacketSendEvent) {
        val player = event.getPlayer() as? BukkitPlayer ?: return
        val entityId =
            when (event.packetType) {
                ENTITY_EQUIPMENT -> WrapperPlayServerEntityEquipment(event).entityId
                ENTITY_ANIMATION -> WrapperPlayServerEntityAnimation(event).entityId
                SPAWN_ENTITY -> WrapperPlayServerSpawnEntity(event).entityId
                ENTITY_VELOCITY -> WrapperPlayServerEntityVelocity(event).entityId
                ENTITY_HEAD_LOOK -> WrapperPlayServerEntityHeadLook(event).entityId
                ENTITY_TELEPORT -> WrapperPlayServerEntityTeleport(event).entityId
                ENTITY_STATUS -> WrapperPlayServerEntityStatus(event).entityId
                ENTITY_METADATA -> WrapperPlayServerEntityMetadata(event).entityId
                ENTITY_EFFECT -> WrapperPlayServerEntityEffect(event).entityId
                REMOVE_ENTITY_EFFECT -> WrapperPlayServerRemoveEntityEffect(event).entityId
                else -> return
            }

        if (!plugin.entityHider.isVisible(player, entityId)) {
            event.setCancelled(true)
        }
    }

    // check when a player is trying to attack a disguise
    override fun onPacketReceive(event: PacketReceiveEvent) {
        val attacker = event.getPlayer() as? BukkitPlayer ?: return

        // we want interact event
        if (event.packetType != INTERACT_ENTITY) return

        val packet = WrapperPlayClientInteractEntity(event)

        // attacking only
        val action = packet.action ?: return
        if (action != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return

        val disguise =
            plugin.disguiser.getByEntityId(packet.entityId)
                ?: plugin.disguiser.getByHitboxId(packet.entityId)
                ?: return
        val player = disguise.player ?: return

        if (player.gameMode == GameMode.CREATIVE) return

        event.setCancelled(true)
        handleAttack(disguise, player, attacker)
    }

    private fun handleAttack(disguise: Disguise, player: BukkitPlayer, seeker: BukkitPlayer) {
        if (player.uniqueId == seeker.uniqueId) return

        val fallbackAmount = 7.0
        val amount =
            if (plugin.shim.supports(9)) {
                val attribName =
                    if (plugin.shim.supports(21)) "ATTACK_DAMAGE" else "GENERIC_ATTACK_DAMAGE"
                val attrib = Attribute.valueOf(attribName)
                seeker.getAttribute(attrib)?.value ?: fallbackAmount
            } else {
                fallbackAmount // uhhh i dunno how to do this for 1.8
            }

        val debounceUUID = player.uniqueId

        disguise.shouldBeSolid = false
        if (debounce.contains(debounceUUID)) return

        // trigger an attack event
        val khsPlayer = BukkitKhsPlayer(plugin.shim, player)
        val khsSeeker = BukkitKhsPlayer(plugin.shim, seeker)
        val khsEvent = DamageEvent(plugin.khs, khsPlayer, khsSeeker, amount)

        // make sure this is run synchronously,
        // otherwise we can get 'EntityRemoveEvent may only be triggered synchronously'
        plugin.shim.scheduleEvent(1UL) {
            // make sure the players are still valid
            if (player.isOnline && seeker.isOnline) onDamage(khsEvent)
        }

        // set and soon turn off debounce
        debounce.add(debounceUUID)
        plugin.server.scheduler.scheduleSyncDelayedTask(
            plugin,
            { debounce.remove(debounceUUID) },
            10,
        )
    }
}
