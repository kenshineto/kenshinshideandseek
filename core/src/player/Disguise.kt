package cat.freya.khs.player

abstract class Disguise {
    @Volatile var isSolid: Boolean = false
    @Volatile var shouldBeSolid: Boolean = false

    abstract fun remove()
}
