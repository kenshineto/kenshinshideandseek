package cat.freya.khs.type

@JvmInline
value class Id(val inner: String) {
    fun namespace(): String? {
        val parts = inner.split(':')
        if (parts.size == 1) return null
        return parts[0]
    }

    fun path(): String {
        val parts = inner.split(':')
        if (parts.size == 1) return parts[0]
        return parts[1]
    }

    fun changePath(newPath: String): Id {
        val namespace = namespace()
        if (namespace != null) {
            return Id("${namespace}:${newPath}")
        } else {
            return Id(newPath)
        }
    }

    override fun toString(): String {
        return inner
    }
}
