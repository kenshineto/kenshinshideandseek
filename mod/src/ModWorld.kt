package cat.freya.khs.mod

import cat.freya.khs.world.Location
import cat.freya.khs.world.Position
import cat.freya.khs.world.World
import cat.freya.khs.world.isMapSave
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.dimension.DimensionType

class ModWorldBorder(val level: ServerLevel) : World.Border {
    private val border = level.worldBorder

    override val x: Double
        get() = border.centerX

    override val z: Double
        get() = border.centerZ

    override val size: Double
        get() = border.size

    override fun move(newX: Double, newZ: Double, newSize: ULong, delay: ULong) {
        border.setCenter(newX, newZ)
        move(newSize, delay)
    }

    override fun move(newSize: ULong, delay: ULong) {
        border.lerpSizeBetween(size, newSize.toDouble(), delay.toLong(), level.gameTime)
    }
}

class ModWorld(val mod: KhsMod, val inner: ServerLevel) : World {
    override val name = inner.dimension().identifier().toString()

    override val type: World.Type = getKhsWorldType()

    private fun getDimensionType(): ResourceKey<DimensionType>? {
        val registry = mod.server.inner.registryAccess().lookupOrThrow(Registries.DIMENSION_TYPE)
        return registry.getResourceKey(inner.dimensionType()).orElse(null)
    }

    private fun getKhsWorldType(): World.Type {
        if (inner.isFlat) return World.Type.FLAT

        return when (getDimensionType()) {
            BuiltinDimensionTypes.OVERWORLD -> World.Type.NORMAL
            BuiltinDimensionTypes.NETHER -> World.Type.NETHER
            BuiltinDimensionTypes.END -> World.Type.END
            else -> World.Type.UNKNOWN
        }
    }

    override val border = ModWorldBorder(inner)

    override fun getSpawn(): Location {
        val pos = inner.respawnData.pos()
        return Location(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), name, 0f, 0f)
    }

    override fun playSound(position: Position, sound: String, volume: Double, pitch: Double) {
        val id = Identifier.tryParse(sound) ?: return
        val holder = BuiltInRegistries.SOUND_EVENT.get(id).orElse(null) ?: return

        inner.playSound(
            null,
            position.x,
            position.y,
            position.z,
            holder,
            SoundSource.AMBIENT,
            volume.toFloat(),
            pitch.toFloat(),
        )
    }

    override fun unload() {
        val levelManager = mod.server.levelManager()
        levelManager.remove(inner)

        inner.players().forEach { modPlayer ->
            val player = ModPlayer(mod, modPlayer)
            player.teleport(mod.khs.config.exit)
        }

        if (!isMapSave(name)) {
            save()
        }
    }

    override fun save() {
        inner.chunkSource.dataStorage.saveAndJoin()
        inner.save(null, true, inner.noSave)
    }

    override fun toString(): String {
        return "ModWorld[$name,$type]"
    }

    companion object {
        fun createLevel(mod: KhsMod, worldName: String, type: World.Type): ServerLevel? {
            val id = Identifier.tryParse(worldName)
            if (id == null) {
                mod.shim.logger.warning("invalid world name: ${worldName}")
                return null
            }

            val key = ResourceKey.create(Registries.DIMENSION, id)
            val levelManager = mod.server.levelManager()
            return levelManager.create(key, type)
        }
    }
}
