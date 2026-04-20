package cat.freya.khs.mod

import cat.freya.khs.math.Vector
import cat.freya.khs.type.Effect
import cat.freya.khs.type.ResourceKey
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Location
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Relative
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team

const val KHS_COLLISION_TEAM_NAME = "KHS_Collision"

open class ModEntity(val mod: KhsMod, private val inner: net.minecraft.world.entity.Entity) : Entity {
    override val entityId = inner.id
    override val uuid = inner.uuid

    private val entityType = EntityType.getKey(inner.type)
    override val type = ResourceKey(entityType.toString(), null, entityType.toString())

    override fun isAlive(): Boolean {
        return inner.isAlive
    }

    override fun getLocation(): Location {
        val worldName = getWorld().name
        return Location(inner.x, inner.y, inner.z, worldName)
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

        val loader = mod.shim.getWorldLoader(location.worldName)
        val world = loader.load() ?: return

        val relative = Relative.union(Relative.DELTA, Relative.ROTATION)
        inner.teleportTo(world.inner, location.x, location.y, location.z, relative, 0f, 0f, false)
    }

    private fun getCollidesTeam(): PlayerTeam {
        val scoreboard = mod.server.inner.scoreboard
        val team =
            scoreboard.getPlayerTeam(KHS_COLLISION_TEAM_NAME)
                ?: scoreboard.addPlayerTeam(KHS_COLLISION_TEAM_NAME)

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
        val living = inner as? LivingEntity ?: return
        val attribute = living.getAttribute(Attributes.MOVEMENT_SPEED)

        if (attribute != null) attribute.baseValue = 0.1 * amplifier.toFloat()
    }

    override fun destroy() {
        val reason = net.minecraft.world.entity.Entity.RemovalReason.DISCARDED
        inner.remove(reason)
    }
}
