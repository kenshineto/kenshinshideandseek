package cat.freya.khs.fabric

import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModPlatform
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.dimension.DimensionType
import xyz.nucleoid.fantasy.Fantasy
import xyz.nucleoid.fantasy.RuntimeLevelConfig
import xyz.nucleoid.fantasy.RuntimeLevelHandle

object KhsModFabric : ModInitializer, ModPlatform("Fabric") {
    lateinit var mod: KhsMod

    private val handles = mutableMapOf<ResourceKey<Level>, RuntimeLevelHandle>()

    override fun onInitialize() {
        mod = KhsMod(this)
    }

    override fun createLevel(
        key: ResourceKey<Level>,
        dimension: ResourceKey<DimensionType>,
        generator: ChunkGenerator,
    ): ServerLevel? {
        val server = mod.server.inner
        val fantasy = Fantasy.get(server)

        val levelConfig = RuntimeLevelConfig().setDimensionType(dimension).setGenerator(generator)

        val levelHandle = fantasy.getOrOpenPersistentLevel(key.identifier(), levelConfig)
        handles.put(key, levelHandle)

        return levelHandle.asLevel()
    }

    override fun unloadLevel(key: ResourceKey<Level>) {
        val levelHandle = handles.get(key) ?: return
        levelHandle.delete()
    }
}
