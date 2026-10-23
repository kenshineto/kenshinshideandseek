package cat.freya.khs

private const val SNAPSHOT_SUFFIX = "-SNAPSHOT"

data class Version(val inner: String) {
    val isSnapshot: Boolean = inner.contains(SNAPSHOT_SUFFIX)
    val parts: IntArray = inner.removeSuffix(SNAPSHOT_SUFFIX).split(".").mapNotNull(String::toIntOrNull).toIntArray()

    operator fun compareTo(other: Version): Int {
        return this.compareTo(other.parts)
    }

    operator fun compareTo(other: IntArray): Int {
        val diff = this.parts.zip(other).firstOrNull { (c, l) -> c != l }?.let { (c, l) -> c - l }
        return diff ?: (this.parts.size - other.size)
    }

    override fun toString(): String {
        return inner
    }
}
