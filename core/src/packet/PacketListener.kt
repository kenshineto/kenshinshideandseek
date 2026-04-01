package cat.freya.khs.packet

import cat.freya.khs.Khs
import cat.freya.khs.disguise.Disguise
import cat.freya.khs.event.DamageEvent
import cat.freya.khs.event.onDamage
import cat.freya.khs.player.Player
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client.INTERACT_ENTITY
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server.*
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity
import com.github.retrooper.packetevents.wrapper.play.server.*
import java.util.UUID

class KhsPacketListener(val plugin: Khs) : PacketListener {
    private val debounce = mutableSetOf<UUID>()
    private val api = PacketEvents.getAPI()

    init {
        api.eventManager.registerListener(this, PacketListenerPriority.NORMAL)
    }

    // intercept entity-related packets of entities that
    // are supposed to be hidden
    override fun onPacketSend(event: PacketSendEvent) {
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

        val player = plugin.shim.wrapPlayer(event.getPlayer()) ?: return
        if (!plugin.entityHider.isVisible(player, entityId)) {
            event.setCancelled(true)
        }
    }

    // check when a player is trying to attack a disguise
    override fun onPacketReceive(event: PacketReceiveEvent) {
        val attacker = plugin.shim.wrapPlayer(event.getPlayer()) ?: return

        // we want interact event
        if (event.packetType != INTERACT_ENTITY) return

        val packet = WrapperPlayClientInteractEntity(event)

        // attacking only
        val action = packet.action ?: return
        if (action != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return

        val disguise =
            plugin.disguiser.getByBlockId(packet.entityId)
                ?: plugin.disguiser.getByHitBoxId(packet.entityId)
                ?: return
        val player = disguise.player ?: return

        if (player.gameMode == Player.GameMode.CREATIVE) return

        event.setCancelled(true)
        handleAttack(disguise, player, attacker)
    }

    private fun handleAttack(disguise: Disguise, player: Player, seeker: Player) {
        if (player.uuid == seeker.uuid) return

        // player has been hit/found
        disguise.shouldBeSolid = false

        val debounceUUID = player.uuid
        if (debounce.contains(debounceUUID)) return

        // caculate damage amount
        val fallbackAmount = 7.0
        // val amount =
        //    if (plugin.shim.supports(9)) {
        //        val attribName =
        //            if (plugin.shim.supports(21)) "ATTACK_DAMAGE" else "GENERIC_ATTACK_DAMAGE"
        //        val attrib = Attribute.valueOf(attribName)
        //        seeker.getAttribute(attrib)?.value ?: fallbackAmount
        //    } else {
        //        fallbackAmount // uhhh i dunno how to do this for 1.8
        //    }
        val amount = fallbackAmount // TODO:

        // trigger an attack event
        val khsEvent = DamageEvent(plugin, player, seeker, amount)

        // make sure this is run synchronously,
        // otherwise we can get 'EntityRemoveEvent may only be triggered synchronously'
        plugin.shim.scheduleEvent(1UL) { onDamage(khsEvent) }

        // set and soon turn off debounce
        debounce.add(debounceUUID)
        plugin.shim.scheduleEvent(10UL) { debounce.remove(debounceUUID) }
    }
}
