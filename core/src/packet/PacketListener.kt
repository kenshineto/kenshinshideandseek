package cat.freya.khs.packet

import cat.freya.khs.Khs
import cat.freya.khs.world.Player
import cat.freya.khs.world.toPosition
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
        val player = plugin.shim.wrapPlayer(event.getPlayer()) ?: return false
        val cause: Player? =
            when (event.packetType) {
                ENTITY_SOUND_EFFECT -> {
                    val packet = WrapperPlayServerEntitySoundEffect(event)
                    plugin.shim.getPlayers().firstOrNull { it.entityId == packet.entityId }
                }
                SOUND_EFFECT -> {
                    val packet = WrapperPlayServerSoundEffect(event)
                    val sound = packet.sound.toString().lowercase()

                    val blockedSounds = setOf("step", "ladder", "fall", "swim", "splash")
                    if (blockedSounds.none { sound.contains(it) }) {
                        return false
                    }

                    plugin.shim
                        .getPlayers()
                        .filter {
                            player.uuid != it.uuid
                        }.minByOrNull {
                            val pos1 = packet.position.toPosition()
                            val pos2 = player.getLocation().toPosition()
                            pos1.distance(pos2)
                        }
                }
                else -> return false
            }

        if (cause == null) return false

        return plugin.game.teams.isSpectator(cause.uuid)
    }

    private fun debugEntityMetadata(event: PacketSendEvent) {
        if (event.packetType != ENTITY_METADATA) return

        val packet = WrapperPlayServerEntityMetadata(event)

        println("DEBUG METADATA: ${packet.entityId}")
        for (data in packet.entityMetadata) {
            println("${data.index} ${data.type} ${data.value}")
        }
    }

    override fun onPacketSend(event: PacketSendEvent) {
        val canceled = handleHiddenEntity(event) || handleSpectatorSound(event)
        event.isCancelled = canceled

        // debugEntityMetadata(event)
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
        if (!plugin.game.teams.isSpectator(player.uuid)) return

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
