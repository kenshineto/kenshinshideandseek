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
    private fun handleHiddenEntity(event: PacketSendEvent): Boolean {
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
                else -> return false
            }

        val player = plugin.shim.wrapPlayer(event.getPlayer()) ?: return false
        return plugin.entityHider.isHidden(player.uuid, entityId)
    }

    // dont allow spectators to make sounds
    // sadly this does not include the punch sound
    private fun handleSpectatorSound(event: PacketSendEvent): Boolean {
        if (event.packetType != ENTITY_SOUND_EFFECT) return false

        val packet = WrapperPlayServerEntitySoundEffect(event)
        val entityId = packet.entityId
        val player =
            plugin.shim.getPlayers().firstOrNull { it.entityId == entityId } ?: return false
        return plugin.game.isSpectator(player)
    }

    override fun onPacketSend(event: PacketSendEvent) {
        val canceled = handleHiddenEntity(event) || handleSpectatorSound(event)
        event.isCancelled = canceled
    }

    // we need to get the players
    // client information
    private fun saveClientSettings(event: PacketReceiveEvent) {
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

    private fun stopSpectatorInteract(event: PacketReceiveEvent) {
        val player = plugin.shim.wrapPlayer(event.getPlayer()) ?: return
        if (!plugin.game.isSpectator(player)) return

        val canceled =
            when (event.packetType) {
                Play.Client.INTERACT_ENTITY -> true
                else -> false
            }

        event.isCancelled = canceled
    }

    override fun onPacketReceive(event: PacketReceiveEvent) {
        saveClientSettings(event)
        stopSpectatorInteract(event)
    }
}
