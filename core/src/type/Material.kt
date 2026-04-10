package cat.freya.khs.type

/**
 * Represents a material/type for a minecraft block/item It holds both the names for the minecraft
 * type, and the platform type
 */
interface Material {
    /** The platform/native mc name of this type */
    val key: ResourceKey

    /** If this material represents a block */
    val isBlock: Boolean

    /** If this material represents an item */
    val isItem: Boolean
}
