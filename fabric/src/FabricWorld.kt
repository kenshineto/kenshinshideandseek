package cat.freya.khs.fabric

import cat.freya.khs.world.AbstractWorld
import cat.freya.khs.world.Location
import cat.freya.khs.world.World
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

class FabricWorldBorder(val level: ServerLevel) : World.Border {
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

class FabricWorldLoader(val mod: KhsMod, override val name: String) :
    World.AbstractLoader(name, name, mod.server.getWorldContainer()) {

    override fun load(): FabricWorld? {
        // TODO:
        return null
    }

    override fun unload() {
        // TODO:
    }
}

class FabricWorld(val mod: KhsMod, val inner: ServerLevel) : AbstractWorld(mod.shim) {

    override val name = inner.toString() // toString calls serverLevelData.levelName

    override val type: World.Type = getTypeImpl()

    private fun getTypeImpl(): World.Type {
        if (inner.isFlat) return World.Type.FLAT

        val dim = inner.dimension()
        return when (dim) {
            Level.OVERWORLD -> World.Type.NORMAL
            Level.NETHER -> World.Type.NETHER
            Level.END -> World.Type.END
            else -> World.Type.UNKNOWN
        }
    }

    override val border = FabricWorldBorder(inner)

    override val loader = FabricWorldLoader(mod, name)

    override fun getSpawn(): Location {
        val pos = inner.respawnData.pos()
        return Location(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), name)
    }
}
