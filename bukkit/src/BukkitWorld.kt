package cat.freya.khs.bukkit

import cat.freya.khs.world.Location
import cat.freya.khs.world.Position
import cat.freya.khs.world.World
import cat.freya.khs.world.isMapSave
import com.cryptomorin.xseries.XSound
import java.util.Random
import org.bukkit.GameRule
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.block.Biome
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator

private class VoidGenerator : ChunkGenerator() {
    // 1.14 And On
    override fun getDefaultPopulators(world: org.bukkit.World): List<BlockPopulator> {
        return listOf()
    }

    override fun canSpawn(world: org.bukkit.World, x: Int, z: Int): Boolean {
        return true
    }

    override fun getFixedSpawnLocation(
        world: org.bukkit.World,
        random: Random,
    ): org.bukkit.Location {
        return org.bukkit.Location(world, 0.0, 0.0, 0.0)
    }

    // 1.13 And Prev
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun generateChunkData(
        world: org.bukkit.World,
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
    @Suppress("UNUSED")
    fun generate(world: org.bukkit.World, random: Random, x: Int, z: Int): ByteArray {
        return ByteArray(world.maxHeight / 16)
    }

    @Suppress("DEPRECATION", "UNUSED")
    fun generateBlockSections(
        world: org.bukkit.World,
        random: Random,
        x: Int,
        z: Int,
        biomes: BiomeGrid,
    ): Array<ByteArray> {
        return Array(world.maxHeight / 16) { ByteArray(0) }
    }
}

class BukkitWorldBorder(val inner: org.bukkit.WorldBorder) : World.Border {
    override val x: Double
        get() = inner.center.x

    override val z: Double
        get() = inner.center.z

    override val size: Double
        get() = inner.size

    override fun move(newX: Double, newZ: Double, newSize: ULong, delay: ULong) {
        inner.setCenter(newX, newZ)
        move(newSize, delay)
    }

    override fun move(newSize: ULong, delay: ULong) {
        inner.setSize(newSize.toDouble(), delay.toLong())
    }
}

class BukkitWorld(val shim: BukkitKhsShim, val inner: org.bukkit.World) : World {
    override val name = inner.name
    override val type: World.Type = getTypeImpl()

    private fun getTypeImpl(): World.Type {
        val env = inner.environment
        if (env == org.bukkit.World.Environment.NETHER) return World.Type.NETHER
        if (env == org.bukkit.World.Environment.THE_END) return World.Type.END

        @Suppress("DEPRECATION")
        return when (inner.worldType) {
            WorldType.NORMAL -> World.Type.NORMAL
            WorldType.FLAT -> World.Type.FLAT
            else -> World.Type.UNKNOWN
        }
    }

    override val border = BukkitWorldBorder(inner.worldBorder)

    override fun getSpawn(): Location {
        val loc = inner.spawnLocation
        return Location(loc.x, loc.y, loc.z, name, loc.yaw, loc.pitch)
    }

    @Suppress("UnstableApiUsage")
    override fun playSound(position: Position, sound: String, volume: Double, pitch: Double) {
        val location = org.bukkit.Location(inner, position.x, position.y, position.z)
        XSound.REGISTRY.getByName(sound).ifPresent {
            it.play(location, volume.toFloat(), pitch.toFloat())
        }
    }

    override fun unload() {
        val plugin = shim.plugin
        inner.players.forEach { bukkitPlayer ->
            val player = BukkitPlayer(plugin, bukkitPlayer)
            player.teleport(plugin.khs.config.exit)
        }

        if (!plugin.server.unloadWorld(name, false)) {
            plugin.shim.logger.error("could not unload world: $name")
        }
    }

    companion object {
        fun folderNameToWorldName(shim: BukkitKhsShim, folderName: String): String {
            if (!shim.usingModernDimensionFormat()) return folderName

            return when (folderName) {
                "overworld" -> "world"
                "the_nether" -> "world_nether"
                "the_end" -> "world_the_end"
                else -> folderName
            }
        }

        fun worldNameToFolderName(shim: BukkitKhsShim, worldName: String): String {
            if (!shim.usingModernDimensionFormat()) return worldName

            return when (worldName) {
                "world" -> "overworld"
                "world_nether" -> "the_nether"
                "world_the_end" -> "the_end"
                else -> worldName
            }
        }

        fun create(plugin: KhsPlugin, worldName: String, type: World.Type): BukkitWorld? {
            val creator = WorldCreator(worldName)

            if (type != World.Type.UNKNOWN) {
                // specify superflat or normal
                if (type == World.Type.FLAT) {
                    creator.type(WorldType.FLAT)
                } else {
                    creator.type(WorldType.NORMAL)
                }

                // specify level stem
                when (type) {
                    World.Type.NETHER -> creator.environment(org.bukkit.World.Environment.NETHER)
                    World.Type.END -> creator.environment(org.bukkit.World.Environment.THE_END)
                    else -> creator.environment(org.bukkit.World.Environment.NORMAL)
                }
            }

            if (isMapSave(worldName)) {
                // generate empty chunks on a map save (wont be saved)
                creator.generator(VoidGenerator())
            }

            plugin.server.createWorld(creator)
            val world = plugin.server.getWorld(worldName)
            if (world == null) {
                plugin.shim.logger.error("could not create/load world: $worldName")
                return null
            }

            if (isMapSave(worldName)) {
                world.isAutoSave = false
                if (plugin.shim.supports(21, 6)) {
                    world.setGameRule(GameRule.LOCATOR_BAR, false)
                }
            } else {
                world.save()
            }

            return BukkitWorld(plugin.shim, world)
        }
    }
}
