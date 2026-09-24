package cat.freya.khs.mod

import cat.freya.khs.event.ClickEvent
import cat.freya.khs.event.onClick
import cat.freya.khs.game.Board
import cat.freya.khs.math.Vector
import cat.freya.khs.menu.Inventory
import cat.freya.khs.type.BlockType
import cat.freya.khs.world.Location
import cat.freya.khs.world.Player
import it.unimi.dsi.fastutil.ints.IntArrayList
import kotlin.runCatching
import net.luckperms.api.LuckPermsProvider
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.projectile.FireworkRocketEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.FireworkExplosion
import net.minecraft.world.item.component.Fireworks
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.HitResult

class ModPlayer(mod: KhsMod, val inner: ServerPlayer) : ModEntity(mod, inner), Player {
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
        inner.connection.send(ClientboundSetEntityMotionPacket(inner))
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
        val modInv = inv as? ModInventory ?: return
        val title = KhsMod.parseText(modInv.title ?: "")

        // close if inventory already open
        if (inner.containerMenu != inner.inventoryMenu) {
            val packet = ClientboundContainerClosePacket(inner.containerMenu.containerId)
            inner.connection.send(packet)
        }

        val type = modInv.getMenuType()
        val menu = modInv.createMenu(inner)
        val openPacket = ClientboundOpenScreenPacket(menu.containerId, type, title)
        val syncPacket = ClientboundContainerSetContentPacket(menu.containerId, menu.stateId, menu.items, menu.carried)
        inner.connection.send(openPacket)
        inner.connection.send(syncPacket)
        inner.containerMenu = menu

        // handle click events
        val player = this
        menu.listeners.add { slot ->
            val item = modInv.get(slot.toUInt()) ?: return@add false
            val event = ClickEvent(mod.khs, player, modInv, item)
            onClick(event)
            event.cancelled
        }
    }

    override fun closeInventory() {
        inner.closeContainer()
    }

    override fun message(message: String) {
        inner.sendSystemMessage(KhsMod.parseText(message), false)
    }

    override fun actionBar(message: String) {
        inner.sendSystemMessage(KhsMod.parseText(message), true)
    }

    override fun title(title: String, subTitle: String) {
        val titlePacket = ClientboundSetTitleTextPacket(KhsMod.parseText(title))
        inner.connection.send(titlePacket)

        val subTitlePacket = ClientboundSetSubtitleTextPacket(KhsMod.parseText(subTitle))
        inner.connection.send(subTitlePacket)
    }

    override fun playSound(sound: String, volume: Double, pitch: Double) {
        val id = Identifier.tryParse(sound)
        if (id == null) {
            mod.shim.logger.warning("invalid sound id: ${sound}")
            return
        }

        val holder = BuiltInRegistries.SOUND_EVENT.get(id).orElse(null)
        if (holder == null) {
            mod.shim.logger.warning("invalid sound: ${id}")
            return
        }

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

    override fun createDisguise(blockType: BlockType): ModDisguise {
        return ModDisguise(mod, uuid, blockType)
    }

    override fun getAttackDamage(): Double {
        return inner.getAttributeValue(Attributes.ATTACK_DAMAGE)
    }

    override fun getEyePosition(): Location {
        val v = inner.eyePosition
        return Location(v.x, v.y, v.z, getWorld().name, inner.yRot, inner.xRot)
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
            }
        )
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

    override fun getScoreBoard(): ModBoard {
        return mod.server.getPlayerScoreBoard(uuid)
    }

    override fun setScoreBoard(board: Board?) {
        val modBoard = board as? ModBoard ?: return
        mod.server.setScoreBoard(uuid, modBoard)
        modBoard.sendTo(inner)
    }

    override fun taunt() {
        val world = getWorld()
        val pos = getLocation()

        val stack = ItemStack(Items.FIREWORK_ROCKET)
        stack.set(
            DataComponents.FIREWORKS,
            Fireworks(
                4,
                listOf(
                    FireworkExplosion(
                        FireworkExplosion.Shape.STAR,
                        IntArrayList(intArrayOf(0x0000FF)),
                        IntArrayList(),
                        true,
                        true,
                    ),
                    FireworkExplosion(
                        FireworkExplosion.Shape.SMALL_BALL,
                        IntArrayList(intArrayOf(0xFF0000)),
                        IntArrayList(),
                        true,
                        true,
                    ),
                    FireworkExplosion(
                        FireworkExplosion.Shape.LARGE_BALL,
                        IntArrayList(intArrayOf(0xFFFF00)),
                        IntArrayList(),
                        true,
                        true,
                    ),
                ),
            ),
        )

        val firework = FireworkRocketEntity(world.inner, stack, pos.x, pos.y, pos.z, false)

        world.inner.addFreshEntity(firework)
    }

    override fun toString(): String {
        return "ModPlayer[$name]"
    }
}
