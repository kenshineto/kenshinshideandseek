package cat.freya.khs.mod

import cat.freya.khs.config.EffectConfig
import cat.freya.khs.type.Effect
import kotlin.jvm.optionals.getOrNull
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.effect.MobEffectInstance

class ModEffect(val inner: MobEffectInstance, val id: Identifier, override val config: EffectConfig) : Effect {
    private val effect = inner.effect.value()

    override val name = id.toString()

    private val mcType = id.toString()
    override val platformType = mcType

    override fun toString(): String {
        return "ModEffect[$platformType]"
    }

    companion object {
        fun parse(config: EffectConfig): ModEffect? {
            val id = Identifier.tryParse(config.type) ?: return null
            val effect = BuiltInRegistries.MOB_EFFECT.get(id).getOrNull() ?: return null

            val ticks = config.duration.toInt() * 20
            val level = config.amplifier.toInt()
            val instance = MobEffectInstance(effect, ticks, level - 1, config.ambient, config.particles, true)
            return ModEffect(instance, id, config)
        }
    }
}
