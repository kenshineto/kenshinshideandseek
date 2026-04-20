package cat.freya.khs.mod

import cat.freya.khs.disguise.Disguise
import cat.freya.khs.game.Board
import cat.freya.khs.math.Vector
import cat.freya.khs.menu.Inventory
import cat.freya.khs.type.Material
import cat.freya.khs.world.Location
import cat.freya.khs.world.Player
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import net.luckperms.api.LuckPermsProvider
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.HitResult
import net.minecraft.world.scores.DisplaySlot
import kotlin.runCatching

class ModPlayer(mod: KhsMod, val inner: ServerPlayer) :
    ModEntity(mod, inner),
    Player {
    override val name: String = inner.name.string

    override fun getHandle(): Any {
        return inner
    }

    override fun getHealth(): Double {
        return inner.health.toDouble()
    }

    override fun setHealth(health: Double) {
        inner.health = health.toFloat()
    }

    override fun heal() {
        inner.health = inner.maxHealth
    }

    override fun getHunger(): UInt {
        return inner.foodData.foodLevel.toUInt()
    }

    fun setHunger(hunger: UInt) {
        inner.foodData.foodLevel = hunger.toInt()
    }

    override fun satiate() {
        setHunger(20u)
    }

    override fun knockBack(direction: Vector) {
        inner.setDeltaMovement(
            direction.x * 0.4,
            0.4,
            direction.z * 0.4,
        )
        inner.hurtMarked = true
    }

    override fun getAllowedFlight(): Boolean {
        return inner.abilities.mayfly
    }

    override fun setAllowedFlight(allowedFlight: Boolean) {
        inner.abilities.mayfly = allowedFlight
    }

    override fun getFlying(): Boolean {
        return inner.abilities.flying
    }

    override fun setFlying(flying: Boolean) {
        inner.abilities.flying = flying
        inner.onUpdateAbilities()
    }

    override fun getInventory(): ModPlayerInventory {
        return ModPlayerInventory(mod.shim, inner)
    }

    override fun showInventory(inv: Inventory) {
        val fabricInv = inv as? ModInventory ?: return
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
        val packet =
            ClientboundSoundPacket(
                holder,
                SoundSource.AMBIENT,
                inner.x,
                inner.y,
                inner.z,
                volume.toFloat(),
                pitch.toFloat(),
                inner.level().seed,
            )
        inner.connection.send(packet)
    }

    override fun createDisguise(material: Material): Disguise? {
        // TODO:
        return null
    }

    override fun getAttackDamage(): Double {
        return inner.getAttributeValue(Attributes.ATTACK_DAMAGE)
    }

    override fun getEyePosition(): Location {
        val v = inner.eyePosition
        return Location(v.x, v.y, v.z, getWorld().name)
    }

    override fun getEyeDirection(): Vector {
        val v = inner.lookAngle
        return Vector(v.x, v.y, v.z)
    }

    override fun getReach(maxReach: Double): Double? {
        val hit = inner.pick(maxReach, 0.0F, false)

        if (hit.type != HitResult.Type.BLOCK) {
            return null
        }

        return inner.eyePosition.distanceTo(hit.location)
    }

    override fun getGameMode(): Player.GameMode {
        return when (inner.gameMode.gameModeForPlayer) {
            GameType.SURVIVAL -> Player.GameMode.SURVIVAL
            GameType.CREATIVE -> Player.GameMode.CREATIVE
            GameType.ADVENTURE -> Player.GameMode.ADVENTURE
            GameType.SPECTATOR -> Player.GameMode.SPECTATOR
        }
    }

    override fun setGameMode(gameMode: Player.GameMode) {
        inner.setGameMode(
            when (gameMode) {
                Player.GameMode.SURVIVAL -> GameType.SURVIVAL
                Player.GameMode.CREATIVE -> GameType.CREATIVE
                Player.GameMode.ADVENTURE -> GameType.ADVENTURE
                Player.GameMode.SPECTATOR -> GameType.SPECTATOR
            },
        )
    }

    private fun isOperator(): Boolean {
        return mod.server.inner.playerList
            .isOp(inner.nameAndId())
    }

    override fun hasPermission(permission: String): Boolean {
        val api = runCatching { LuckPermsProvider.get() }.getOrElse { null }
        val default = isOperator()

        val user = api?.userManager?.getUser(inner.getUUID())
        val hasPerm =
            user
                ?.cachedData
                ?.permissionData
                ?.checkPermission(permission)
                ?.asBoolean()

        return hasPerm ?: default
    }

    override fun getScoreBoard(): ModBoard {
        return mod.server.getScoreBoard(uuid)
    }

    override fun setScoreBoard(board: Board?) {
        val fabricBoard = board as? ModBoard ?: return
        val objective = mod.server.setScoreBoard(this, fabricBoard)
        val packet = ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, objective)
        inner.connection.send(packet)
    }

    override fun taunt() {
        // TODO:
    }

    override fun sendPacket(packet: PacketWrapper<*>) {
        PacketEvents.getAPI().playerManager.sendPacket(inner, packet)
    }
}
