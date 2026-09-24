package cat.freya.khs.mod

import cat.freya.khs.config.EffectConfig
import cat.freya.khs.math.Vector
import cat.freya.khs.type.Effect
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Location
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.portal.TeleportTransition
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team

const val KHS_COLLISION_TEAM_NAME = "KHS_Collision"

open class ModEntity(val mod: KhsMod, private val inner: net.minecraft.world.entity.Entity) : Entity {
    override val entityId = inner.id
    override val uuid = inner.uuid

    override val mcType = EntityType.getKey(inner.type).toString()

    override fun isAlive(): Boolean {
        return inner.isAlive
    }

    override fun getLocation(): Location {
        val worldName = getWorld().name
        return Location(inner.x, inner.y, inner.z, worldName, inner.yRot, inner.xRot)
    }

    override fun getPitch(): Float {
        return inner.xRot
    }

    override fun getYaw(): Float {
        return inner.yRot
    }

    override fun getHeadYaw(): Float? {
        val living = inner as? LivingEntity ?: return null
        return living.yHeadRot
    }

    override fun getVelocity(): Vector {
        val v = inner.deltaMovement
        return Vector(v.x, v.y, v.z)
    }

    override fun getWorld(): ModWorld {
        return ModWorld(mod, inner.level() as ServerLevel)
    }

    override fun teleport(location: Location?) {
        if (location == null) return

        val world = mod.khs.loadWorld(location.worldName) as? ModWorld ?: return
        val transition =
            TeleportTransition(
                world.inner,
                Vec3(location.x, location.y, location.z),
                Vec3.ZERO,
                location.yaw,
                location.pitch,
                TeleportTransition.DO_NOTHING,
            )
        inner.teleport(transition)
    }

    private fun getCollidesTeam(): PlayerTeam {
        val scoreboard = mod.server.inner.scoreboard
        val team =
            scoreboard.getPlayerTeam(KHS_COLLISION_TEAM_NAME) ?: scoreboard.addPlayerTeam(KHS_COLLISION_TEAM_NAME)

        team.collisionRule = Team.CollisionRule.NEVER
        team.setSeeFriendlyInvisibles(false)
        return team
    }

    override fun setCollides(collides: Boolean) {
        val team = getCollidesTeam()
        val id = inner.getUUID().toString()
        if (collides) {
            team.players.remove(id)
        } else {
            team.players.add(id)
        }
    }

    override fun giveEffect(effect: Effect) {
        val living = inner as? LivingEntity ?: return
        val wrapper = effect as? ModEffect ?: return
        living.addEffect(wrapper.inner)
    }

    override fun clearEffects() {
        val living = inner as? LivingEntity ?: return
        living.removeAllEffects()
    }

    override fun setSpeed(amplifier: UInt) {
        val config =
            EffectConfig(
                type = "minecraft:speed",
                duration = 1000000u,
                amplifier = amplifier,
                ambient = false,
                particles = false,
            )
        val effect = ModEffect.parse(config) ?: return
        giveEffect(effect)
    }

    override fun destroy() {
        val reason = net.minecraft.world.entity.Entity.RemovalReason.DISCARDED
        inner.remove(reason)
    }

    override fun toString(): String {
        return "ModEntity[$entityId,$mcType]"
    }
}
