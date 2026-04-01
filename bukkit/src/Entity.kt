package cat.freya.khs.bukkit

import cat.freya.khs.world.Entity as KhsEntity
import cat.freya.khs.world.Location
import cat.freya.khs.world.World as KhsWorld
import org.bukkit.entity.Entity as BukkitEntity
import org.bukkit.entity.LivingEntity as BukkitLivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team

const val KHS_COLLISION_TEAM_NAME = "KHS_Collision"

class BukkitKhsEntity(val plugin: KhsPlugin, val inner: BukkitEntity) : KhsEntity {

    override val location: Location
        get() {
            val loc = inner.location
            return Location(loc.x, loc.y, loc.z, inner.world.name.intern())
        }

    override val world: KhsWorld?
        get() = plugin.shim.getWorld(location.worldName)

    override val entityId: Int
        get() = inner.entityId

    override val isAlive: Boolean
        get() = !inner.isDead

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

    override fun setInvisible(invisible: Boolean) {
        val living = inner as? BukkitLivingEntity ?: return
        if (living.hasPotionEffect(PotionEffectType.INVISIBILITY) == invisible) return

        if (invisible)
            living.addPotionEffect(
                PotionEffect(PotionEffectType.INVISIBILITY, 1_000_000, 0, false, false)
            )
        else living.removePotionEffect(PotionEffectType.INVISIBILITY)
    }

    override fun destroy() {
        runCatching { inner.remove() }
    }
}
