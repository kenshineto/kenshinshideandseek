package cat.freya.khs.packet

import cat.freya.khs.Khs
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Configuration
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server.*
import com.github.retrooper.packetevents.wrapper.configuration.client.*
import com.github.retrooper.packetevents.wrapper.play.client.*
import com.github.retrooper.packetevents.wrapper.play.server.*

class KhsPacketListener(val plugin: Khs) : PacketListener {
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
        if (plugin.entityHider.isHidden(player.uuid, entityId)) {
            event.isCancelled = true
        }
    }

    override fun onPacketReceive(event: PacketReceiveEvent) {
        // we need to get the players
        // client information

        val packet =
            when (event.packetType) {
                Play.Client.CLIENT_SETTINGS -> WrapperPlayClientSettings(event)
                Configuration.Client.CLIENT_SETTINGS -> WrapperConfigClientSettings(event)
                else -> return
            }

        val player = plugin.shim.wrapPlayer(event.getPlayer()) ?: return
        val settings = ClientSettings.fromPacket(packet)
        plugin.clientSettings[player.uuid] = settings
    }
}
