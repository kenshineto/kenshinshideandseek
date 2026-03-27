package cat.freya.khs.player

import cat.freya.khs.world.Item

// Inventory wrapper
interface Inventory {
    val title: String?

    // update inventory items
    fun get(index: UInt): Item?

    fun set(index: UInt, item: Item)

    fun remove(item: Item)

    // view into entire inventory
    var contents: List<Item?>

    // removes all items
    fun clear()
}

// Player inventory wrapper
interface PlayerInventory : Inventory {
    // update armor
    var helmet: Item?
    var chestplate: Item?
    var leggings: Item?
    var boots: Item?
}
