package cat.freya.khs.fabric

import cat.freya.khs.config.EffectConfig
import cat.freya.khs.type.Effect
import cat.freya.khs.type.ResourceKey
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.effect.MobEffectInstance
import kotlin.jvm.optionals.getOrNull

class FabricEffect(val inner: MobEffectInstance, override val config: EffectConfig) : Effect {
    private val effect = inner.effect.value()
    private val id = BuiltInRegistries.MOB_EFFECT.getKey(effect) ?: error("could not get effect id")

    override val name = id.toString()
    override val key: ResourceKey = ResourceKey(name, null, name)

    companion object {
        fun parse(config: EffectConfig): FabricEffect? {
            val id = Identifier.parse(config.type)
            val effect = BuiltInRegistries.MOB_EFFECT.get(id).getOrNull() ?: return null

            val ticks = config.duration.toInt() * 20
            val level = config.amplifier.toInt()
            val instance = MobEffectInstance(effect, ticks, level, config.ambient, config.particles)
            return FabricEffect(instance, config)
        }
    }
}
