package cat.freya.khs.mod.internal

class SafeIterator<T>(source: Collection<T>) : Iterator<T> {
    private val values: Array<Any?> = source.toTypedArray()
    private var index = 0

    override fun hasNext(): Boolean {
        return values.size > index
    }

    @Suppress("UNCHECKED_CAST")
    override fun next(): T {
        return values[index++] as T
    }
}
