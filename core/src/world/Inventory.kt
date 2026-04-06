package cat.freya.khs.world

/** Represents an inventory container */
interface Inventory {
    /** The name of the container */
    val title: String?

    /** Get a item from the inventory, returning null if empty or out of bounds */
    fun get(index: UInt): Item?

    /** Set an item in the inventory, doing nothing if out of bounds */
    fun set(index: UInt, item: Item?)

    /** Remove all occourances of the given item from the inventory */
    fun remove(item: Item)

    /** @return all contents in order, including empty, of the inventory */
    fun getContents(): List<Item?>

    /** Set the contents of the inventory */
    fun setContents(contents: List<Item?>)

    /** Clear all contents from the inventory */
    fun clearContents()
}

/** Represents an inventory of a player */
interface PlayerInventory : Inventory {
    /** @return the currently equipped helmet */
    fun getHelmet(): Item?

    /** Set a helmet on the player */
    fun setHelmet(helmet: Item?)

    /** @return the currently equipped helmet */
    fun getChestplate(): Item?

    /** Set a chestplate on the player */
    fun setChestplate(chestplate: Item?)

    /** @return the currently equipped chestplate */
    fun getLeggings(): Item?

    /** Set a pair of leggings on the player */
    fun setLeggings(leggings: Item?)

    /** @return the currently equipped leggings */
    fun getBoots(): Item?

    /** Set the boots on the player */
    fun setBoots(boots: Item?)

    /** Clear all contents and armor from the player */
    fun clearAll() {
        setHelmet(null)
        setChestplate(null)
        setLeggings(null)
        setBoots(null)
        clearContents()
    }
}
