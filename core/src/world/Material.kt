package cat.freya.khs.world

data class Material(val mcName: String?, val platformName: String) {
    override fun toString(): String = platformName
}
