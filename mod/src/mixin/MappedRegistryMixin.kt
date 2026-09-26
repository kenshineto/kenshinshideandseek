package cat.freya.khs.mod.mixin

import cat.freya.khs.mod.internal.RemoveFromRegistry
import it.unimi.dsi.fastutil.objects.ObjectList
import it.unimi.dsi.fastutil.objects.Reference2IntMap
import net.minecraft.core.Holder
import net.minecraft.core.MappedRegistry
import net.minecraft.core.RegistrationInfo
import net.minecraft.core.Registry
import net.minecraft.core.WritableRegistry
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow

@Mixin(MappedRegistry::class)
abstract class MappedRegistryMixin<T : Any> : RemoveFromRegistry<T>, WritableRegistry<T> {
    @Shadow @Final private lateinit var byValue: MutableMap<T, Holder.Reference<T>>

    @Shadow @Final private lateinit var byLocation: MutableMap<Identifier, Holder.Reference<T>>

    @Shadow @Final private lateinit var byKey: MutableMap<ResourceKey<T>, Holder.Reference<T>>

    @Shadow @Final private lateinit var registrationInfos: MutableMap<ResourceKey<T>, RegistrationInfo>

    @Shadow @Final private lateinit var byId: ObjectList<Holder.Reference<T>>

    @Shadow @Final private lateinit var toId: Reference2IntMap<T>

    @Shadow @Final private lateinit var key: ResourceKey<out Registry<T>>

    @Shadow private var frozen: Boolean = false

    override fun `khs$remove`(entry: T): Boolean {
        val registryEntry = byValue[entry]
        val rawId = toId.removeInt(entry)

        if (rawId == -1 || registryEntry == null) {
            return false
        }

        return runCatching {
            byKey.remove(registryEntry.key())
            byLocation.remove(registryEntry.key().identifier())
            byValue.remove(entry)
            byId[rawId] = null
            @Suppress("UNCHECKED_CAST") registrationInfos.remove(key as ResourceKey<T>)
        }
            .isSuccess
    }

    override fun `khs$remove`(key: Identifier): Boolean {
        val entry = byLocation[key]

        return entry != null && entry.isBound && `khs$remove`(entry.value())
    }

    override fun `khs$setFrozen`(value: Boolean) {
        frozen = value
    }

    override fun `khs$isFrozen`(): Boolean {
        return frozen
    }
}
