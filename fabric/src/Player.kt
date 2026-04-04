package cat.freya.khs.fabric

import cat.freya.khs.player.Player as KhsPlayer
import cat.freya.khs.player.PlayerInventory as KhsPlayerInventory
import cat.freya.khs.player.Inventory as KhsInventory
import cat.freya.khs.disguise.Disguise
import cat.freya.khs.world.World as KhsWorld
import cat.freya.khs.world.Effect
import cat.freya.khs.world.Position
import cat.freya.khs.world.Location
import cat.freya.khs.world.Material
import cat.freya.khs.fabric.mixin.MixinChunkMap
import cat.freya.khs.fabric.FabricKhsPlayerInventory
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerPlayerGameMode
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Relative
import net.minecraft.world.entity.Entity
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.level.GameType
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundSource
import net.luckperms.api.LuckPermsProvider
import java.util.UUID
import kotlin.runCatching
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.PacketWrapper

class FabricKhsPlayer(val mod: KhsMod, val inner: ServerPlayer): KhsPlayer {

    override val uuid: UUID = inner.getUUID()

    override val name: String = inner.name.string

    override var health: Double
        get() = inner.health.toDouble()
        set(value) { inner.health = value.toFloat() }

    override var hunger: UInt
        get() = inner.foodData.foodLevel.toUInt()
        set(value) { inner.foodData.foodLevel = value.toInt() }

    override fun heal() { inner.health = inner.maxHealth }

    override var allowFlight: Boolean
        get() = inner.abilities.mayfly
        set(value) { inner.abilities.mayfly = value }

    override var flying: Boolean
        get() = inner.abilities.flying
        set(value) {
            inner.abilities.flying = value
            inner.onUpdateAbilities()
        }

    override fun teleport(position: Position) {
        inner.teleportTo(position.x, position.y, position.z)
    }

    override fun teleport(location: Location) {
        val world = mod.server.getWorld(location.worldName) ?: return
        val relative = Relative.union(Relative.DELTA, Relative.ROTATION)
        `inner.teleportTo(world.inner, location.x, location.y, location.z, relative, 0f, 0f, false)
    }

    override fun sendToServer(server: String) {
        // TODO: bungeecord
    }

    override val inventory: KhsPlayerInventory
        get() = FabricKhsPlayerInventory(mod.shim, inner.inventory, inner)

    override fun showInventory(inv: KhsInventory) {
        val fabricInv = inv as? FabricKhsInventory ?: return
        val title = Component.literal(fabricInv.title ?: "")

        // close if inventory already open
        if (inner.containerMenu != inner.inventoryMenu) {
            val packet = ClientboundContainerClosePacket(inner.containerMenu.containerId)
            inner.connection.send(packet)
        }

        val type = fabricInv.getMenuType()
        val menu = fabricInv.createMenu(inner)
        val packet = ClientboundOpenScreenPacket(menu.containerId, type, title)
        inner.connection.send(packet)
        inner.containerMenu = menu
        inner.initInventoryMenu()
    }

    override fun closeInventory() {
        inner.closeContainer()
    }

    override fun clearEffects() {
        inner.removeAllEffects()
    }

    override fun giveEffect(effect: Effect) {
        val wrapper = effect as? FabricKhsEffect ?: return
        val ticks = wrapper.config.duration.toInt() * 20
        val level = wrapper.config.amplifier.toInt()
        val instance = MobEffectInstance(wrapper.inner, ticks, level,
            wrapper.config.ambient, wrapper.config.particles)
        inner.addEffect(instance)
    }

    override fun setSpeed(amplifier: UInt) {
        inner.abilities.walkingSpeed = amplifier.toFloat() * 0.1f
        inner.onUpdateAbilities()
    }

    private fun hideEntity(target: Entity) {
        val level = target.level() as ServerLevel
        val tracker = level.chunkSource.chunkMap as MixinChunkMap
        val entry = tracker.entityMap.get(target.id)
        if (entry != null) {
            entry.removePlayer(inner)
        }
    }

    private fun showEntity(target: Entity) {
        val level = target.level() as ServerLevel
        val tracker = level.chunkSource.chunkMap as MixinChunkMap

        if (target is ServerPlayer) {
            val packet = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(listOf(target))
            inner.connection.send(packet)
        }

        val entry = tracker.entityMap.get(target.id)
        if (entry != null && !entry.seenBy.contains(inner.connection)) {
            entry.updatePlayer(inner)
        }
    }

    override fun setHidden(target: KhsPlayer, hidden: Boolean) {
        val player = (target as? FabricKhsPlayer)?.inner ?: return
        if (hidden) { hideEntity(player) } else { showEntity(player) }
    }

    override fun message(message: String) {
        inner.sendSystemMessage(Component.literal(message), false)
    }

    override fun actionBar(message: String) {
        inner.sendSystemMessage(Component.literal(message), true)
    }

    override fun title(title: String, subTitle: String) {
        val titlePacket = ClientboundSetTitleTextPacket(Component.literal(title))
        inner.connection.send(titlePacket)

        val subTitlePacket = ClientboundSetSubtitleTextPacket(Component.literal(subTitle))
        inner.connection.send(subTitlePacket)
    }

    override fun playSound(sound: String, volume: Double, pitch: Double) {
		val id = Identifier.tryParse(name) ?: return
		val holder = BuiltInRegistries.SOUND_EVENT.get(id).orElse(null) ?: return
        val packet = ClientboundSoundPacket(holder,
            SoundSource.AMBIENT,
            inner.x, inner.y, inner.z,
            volume.toFloat(), pitch.toFloat(), inner.level().seed)
        inner.connection.send(packet)
    }

    override fun createDisguise(material: Material): Disguise? {
        // TODO:
        error("todo")
    }

    override var gameMode: KhsPlayer.GameMode
        get() = when(inner.gameMode.gameModeForPlayer) {
            else -> KhsPlayer.GameMode.SURVIVAL
        }
        set(value) {
            inner.setGameMode(when(value) {
                KhsPlayer.GameMode.SURVIVAL -> GameType.SURVIVAL
                KhsPlayer.GameMode.CREATIVE -> GameType.CREATIVE
                KhsPlayer.GameMode.ADVENTURE -> GameType.ADVENTURE
                KhsPlayer.GameMode.SPECTATOR -> GameType.SPECTATOR
            })
        }

    private fun isOperator(): Boolean {
        return mod.server.inner.playerList.isOp(inner.nameAndId())
    }

    override fun hasPermission(permission: String): Boolean {
        val api = runCatching { LuckPermsProvider.get() }.getOrElse { null }
        val default = isOperator()

        val user = api?.userManager?.getUser(inner.getUUID())
        val hasPerm = user?.cachedData?.permissionData?.checkPermission(permission)?.asBoolean()

        return hasPerm ?: default
    }

    override fun hideBoards() {
        // TODO:
    }

    override fun taunt() {
        // TODO:
    }

    override fun sendPacket(packet: PacketWrapper<*>) {
        PacketEvents.getAPI().playerManager.sendPacket(inner, packet)
    }

    override val location: Location
        get() = Location(inner.x, inner.y, inner.z, world?.name ?: error("could not get world"))

    override val world: KhsWorld?
        get() {
            val level = inner.level() ?: return null
            return FabricKhsWorld(mod, level)
        }

    override val entityId: Int
        get() = inner.id

    override val isAlive: Boolean
        get() = inner.isAlive

    override fun setCollides(collides: Boolean) { }

    override fun setInvisible(invisible: Boolean) {
        val holder = MobEffects.INVISIBILITY

        if (!invisible) {
            inner.removeEffect(holder)
            return
        }

        val effect = MobEffectInstance(holder, Integer.MAX_VALUE, 0, false, false)
        inner.addEffect(effect)
    }

    override fun destroy() { /* no */ }

}
