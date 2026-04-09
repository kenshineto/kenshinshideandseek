package cat.freya.khs.bukkit

import cat.freya.khs.world.Effect
import cat.freya.khs.world.Entity
import cat.freya.khs.world.Location
import cat.freya.khs.world.ResourceKey
import cat.freya.khs.world.Vector
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team

const val KHS_COLLISION_TEAM_NAME = "KHS_Collision"

open class BukkitEntity(val plugin: KhsPlugin, private val inner: org.bukkit.entity.Entity) :
    Entity {
    override val entityId = inner.entityId
    override val uuid = inner.uniqueId
    override val type = getResourceKey()

    @Suppress("DEPRECATION")
    private fun getResourceKey(): ResourceKey {
        val bukkitType = inner.type
        val platformKey = bukkitType.name
        val minecraftId = runCatching { bukkitType.getTypeId() }.getOrElse { null }?.toInt()
        val minecraftKey = runCatching { bukkitType.key.toString() }.getOrElse { null }

        return ResourceKey(minecraftKey, minecraftId, platformKey)
    }

    override fun isAlive(): Boolean {
        return !inner.isDead
    }

    override fun getLocation(): Location {
        val loc = inner.location
        return Location(loc.x, loc.y, loc.z, inner.world.name)
    }

    override fun getPitch(): Float {
        return inner.getLocation().pitch
    }

    override fun getYaw(): Float {
        return inner.getLocation().yaw
    }

    override fun getHeadYaw(): Float? {
        val living = inner as? LivingEntity ?: return null
        return living.eyeLocation.yaw
    }

    override fun getVelocity(): Vector {
        val v = inner.velocity
        return Vector(v.x, v.y, v.z)
    }

    override fun getWorld(): BukkitWorld? {
        return plugin.shim.getWorld(inner.world.name)
    }

    override fun teleport(location: Location?) {
        if (location == null) return

        val loader = plugin.shim.getWorldLoader(location.worldName)
        val world = loader.load() ?: return
        val bukkitWorld = (world as? BukkitWorld)?.inner ?: return

        inner.teleport(org.bukkit.Location(bukkitWorld, location.x, location.y, location.z))
    }

    private fun getCollidesTeam(): Team? {
        val scoreboard = plugin.server.scoreboardManager?.mainScoreboard ?: return null
        val team =
            scoreboard.getTeam(KHS_COLLISION_TEAM_NAME)
                ?: scoreboard.registerNewTeam(KHS_COLLISION_TEAM_NAME)
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
        team.setCanSeeFriendlyInvisibles(false)
        return team
    }

    override fun setCollides(collides: Boolean) {
        if (plugin.shim.supports(9)) {
            val team = getCollidesTeam() ?: return
            val id = inner.uniqueId.toString()
            if (collides) team.removeEntry(id) else team.addEntry(id)
        } else {
            val method = inner.spigot().javaClass.getMethod("setCollidesWithEntities")
            method.invoke(inner, collides)
        }
    }

    private fun getBukkitLiving(): LivingEntity? {
        return inner as? LivingEntity
    }

    override fun giveEffect(effect: Effect) {
        val living = getBukkitLiving() ?: return
        val bukkitEffect = (effect as? BukkitEffect)?.inner ?: return
        living.addPotionEffect(bukkitEffect)
    }

    override fun clearEffects() {
        val living = getBukkitLiving() ?: return
        living.activePotionEffects.forEach { living.removePotionEffect(it.type) }
    }

    override fun setSpeed(amplifier: UInt) {
        val living = getBukkitLiving() ?: return
        living.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 1000000, 5, false, false))
    }

    override fun destroy() {
        // this cannot be called async
        if (!plugin.server.isPrimaryThread) {
            plugin.scheduleTask { destroy() }
            return
        }

        runCatching { inner.remove() }
    }
}
