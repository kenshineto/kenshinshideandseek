package cat.freya.khs.mod.internal

import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.Identifier

interface RemoveFromRegistry<T : Any> {
    companion object {
        @JvmStatic
        fun <T : Any> remove(
            registry: MappedRegistry<T>,
            key: Identifier,
        ): Boolean {
            @Suppress("UNCHECKED_CAST")
            return (registry as RemoveFromRegistry<T>).`khs$remove`(key)
        }

        @JvmStatic
        fun <T : Any> remove(
            registry: MappedRegistry<T>,
            entry: T,
        ): Boolean {
            @Suppress("UNCHECKED_CAST")
            return (registry as RemoveFromRegistry<T>).`khs$remove`(entry)
        }

        @JvmStatic
        fun <T : Any> thaw(registry: Registry<T>): RegistryRemoval {
            @Suppress("UNCHECKED_CAST") val registry1 = registry as RemoveFromRegistry<T>

            val priorStateOfMatter = registry1.`khs$isFrozen`()
            registry1.`khs$setFrozen`(false)

            return RegistryRemoval {
                registry1.`khs$setFrozen`(priorStateOfMatter)
            }
        }
    }

    fun `khs$remove`(entry: T): Boolean

    fun `khs$remove`(key: Identifier): Boolean

    fun `khs$setFrozen`(value: Boolean)

    fun `khs$isFrozen`(): Boolean

    fun interface RegistryRemoval : AutoCloseable {
        override fun close()
    }
}
