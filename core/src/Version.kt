package cat.freya.khs

private const val SNAPSHOT_SUFFIX = "-SNAPSHOT"

data class Version(val inner: String) : Comparable<Version> {
    val isSnapshot: Boolean = inner.contains(SNAPSHOT_SUFFIX)
    val parts: List<UInt> = inner.removeSuffix(SNAPSHOT_SUFFIX).split(".").mapNotNull(String::toUIntOrNull)

    override fun compareTo(other: Version): Int {
        val diff = this.parts.zip(other.parts).firstOrNull { (c, l) -> c != l }?.let { (c, l) -> c - l }
        return diff?.toInt() ?: (this.parts.size - other.parts.size)
    }

    override fun toString(): String {
        return inner
    }

    fun supports(vararg reqParts: Int): Boolean {
        val other = Version.build(*reqParts)
        return this >= other
    }

    companion object {
        fun build(vararg parts: Int): Version {
            return Version(parts.joinToString(separator = "."))
        }

        fun buildMc(vararg parts: Int): Version {
            if (parts[0] < 26) {
                // pre 26.1 releases start with a 1.
                return build(1, *parts)
            }

            return build(*parts)
        }
    }
}
