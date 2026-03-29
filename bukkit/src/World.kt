package cat.freya.khs.bukkit

import cat.freya.khs.world.Position
import cat.freya.khs.world.World as KhsWorld
import cat.freya.khs.world.World.Border as KhsWorldBorder
import cat.freya.khs.world.World.Loader as KhsWorldLoader
import java.io.File
import java.util.Random
import org.bukkit.GameRule
import org.bukkit.Location as BukkitLocation
import org.bukkit.World as BukkitWorld
import org.bukkit.WorldBorder as BukkitWorldBorder
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.block.Biome
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator

class VoidGenerator : ChunkGenerator() {
    // 1.14 And On
    override fun getDefaultPopulators(world: BukkitWorld): List<BlockPopulator> {
        return listOf()
    }

    override fun canSpawn(world: BukkitWorld, x: Int, z: Int): Boolean {
        return true
    }

    override fun getFixedSpawnLocation(world: BukkitWorld, random: Random): BukkitLocation {
        return BukkitLocation(world, 0.0, 0.0, 0.0)
    }

    // 1.13 And Prev
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun generateChunkData(
        world: BukkitWorld,
        random: Random,
        chunkX: Int,
        chunkZ: Int,
        biome: BiomeGrid,
    ): ChunkData {
        val chunkData = createChunkData(world)

        for (x in 0 until 16) for (z in 0 until 16) biome.setBiome(x, z, Biome.PLAINS)

        return chunkData
    }

    // 1.8
    fun generate(world: BukkitWorld, random: Random, x: Int, z: Int): ByteArray {
        return ByteArray(world.maxHeight / 16)
    }

    @Suppress("DEPRECATION")
    fun generateBlockSections(
        world: BukkitWorld,
        random: Random,
        x: Int,
        z: Int,
        biomes: ChunkGenerator.BiomeGrid,
    ): Array<ByteArray> {
        return Array(world.maxHeight / 16) { ByteArray(0) }
    }
}

class BukkitKhsWorldBorder(val world: BukkitKhsWorld, val inner: BukkitWorldBorder) :
    KhsWorldBorder {
    override val x: Double = inner.center.x
    override val z: Double = inner.center.z
    override val size: Double = inner.size

    override fun move(newX: Double, newZ: Double, newSize: ULong, delay: ULong) {
        inner.setCenter(newX, newZ)
        move(newSize, delay)
    }

    override fun move(newSize: ULong, delay: ULong) {
        inner.setSize(newSize.toDouble(), delay.toLong())
    }
}

class BukkitKhsWorldLoader(val plugin: KhsPlugin, val worldName: String) : KhsWorldLoader {
    override val name: String = worldName
    override val world: KhsWorld? = plugin.shim.getWorld(worldName)

    override val dir: File
        get() = File(plugin.server.worldContainer, name)

    override val saveDir: File
        get() = File(plugin.server.worldContainer, "hs_$name")

    override val tempSaveDir: File
        get() = File(plugin.server.worldContainer, "temp_hs_$name")

    override fun load() {
        var creator = WorldCreator(name)
        if (worldName.startsWith("hs_")) creator = creator.generator(VoidGenerator())
        plugin.server.createWorld(creator)
        val world = plugin.server.getWorld(name)
        if (world == null) {
            plugin.shim.logger.error("could not load world: $name")
        }
        world?.setAutoSave(false)
        if (plugin.shim.supports(21, 6)) world?.setGameRule(GameRule.LOCATOR_BAR, false)
    }

    override fun unload() {
        val world = plugin.server.getWorld(name)
        if (world == null) return // world already unloaded

        world.players.forEach { player ->
            val khsPlayer = BukkitKhsPlayer(plugin.shim, player)
            plugin.khs.config.exit?.teleport(khsPlayer)
        }

        if (plugin.server.unloadWorld(name, false) == false)
            plugin.shim.logger.error("could not unload world: $name")
    }

    override fun rollback() {
        unload()
        load()
    }
}

class BukkitKhsWorld(val shim: BukkitKhsShim, val inner: BukkitWorld) : KhsWorld {
    override val name = inner.name
    override val type: KhsWorld.Type
        get() {
            val env = inner.environment
            if (env == BukkitWorld.Environment.NETHER) return KhsWorld.Type.NETHER
            if (env == BukkitWorld.Environment.THE_END) return KhsWorld.Type.END

            @Suppress("DEPRECATION")
            return when (inner.worldType) {
                WorldType.NORMAL -> KhsWorld.Type.NORMAL
                WorldType.FLAT -> KhsWorld.Type.FLAT
                else -> KhsWorld.Type.UNKNOWN
            }
        }

    override val minY: Int
        get() = if (shim.supports(18)) -64 else 0

    override val maxY: Int
        get() = if (shim.supports(18)) 320 else 256

    override val spawn: Position
        get() {
            val loc = inner.spawnLocation
            return Position(loc.x, loc.y, loc.z)
        }

    override val border: KhsWorldBorder
        get() = BukkitKhsWorldBorder(this, inner.worldBorder)

    override val loader: KhsWorldLoader
        get() = shim.getWorldLoader(name)
}
