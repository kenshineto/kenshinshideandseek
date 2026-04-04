package cat.freya.khs.fabric

import cat.freya.khs.world.Position
import cat.freya.khs.world.World as KhsWorld
import java.io.File
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

class FabricKhsWorldBorder(val level: ServerLevel) : KhsWorld.Border {
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

class FabricKhsWorldLoader(val mod: KhsMod, override val name: String) : KhsWorld.Loader {
    override val world: KhsWorld?
        get() = mod.server.getWorld(name)

    override val dir: File
        get() = mod.server.getWorldContainer().resolve(name).toFile()

    override val saveDir: File
        get() = mod.server.getWorldContainer().resolve("hs_$name").toFile()

    override val tempSaveDir: File
        get() = mod.server.getWorldContainer().resolve("temp_hs_$name").toFile()

    private val isMapSave: Boolean = name.startsWith("hs_")

    override fun load() {
        // TODO:
    }

    override fun unload() {
        // TODO:
    }

    override fun rollback() {
        unload()
        load()
    }
}

class FabricKhsWorld(val mod: KhsMod, val inner: ServerLevel) : KhsWorld {

    override val name = inner.toString() // toString calls serverLevelData.levelName

    override val type: KhsWorld.Type
        get() {
            if (inner.isFlat) return KhsWorld.Type.FLAT

            val dim = inner.dimension()
            return when (dim) {
                Level.OVERWORLD -> KhsWorld.Type.NORMAL
                Level.NETHER -> KhsWorld.Type.NETHER
                Level.END -> KhsWorld.Type.END
                else -> KhsWorld.Type.UNKNOWN
            }
        }

    override val minY: Int
        get() = if (mod.shim.supports(18)) -64 else 0

    override val maxY: Int
        get() = if (mod.shim.supports(18)) 320 else 256

    override val spawn: Position
        get() {
            val pos = inner.respawnData.pos()
            return Position(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
        }

    override val border: KhsWorld.Border = FabricKhsWorldBorder(inner)

    override val loader: KhsWorld.Loader = FabricKhsWorldLoader(mod, name)
}
