package cat.freya.khs.bukkit

import cat.freya.khs.player.Inventory as KhsInventory
import cat.freya.khs.player.PlayerInventory as KhsPlayerInventory
import cat.freya.khs.world.Item
import org.bukkit.inventory.Inventory as BukkitInventory
import org.bukkit.inventory.PlayerInventory as BukkitPlayerInventory

open class BukkitKhsInventory(
    open val shim: BukkitKhsShim,
    open val inner: BukkitInventory,
    open override val title: String?,
) : KhsInventory {
    override fun get(index: UInt): Item? = inner.getItem(index.toInt())?.let { toKhsItem(it) }

    override fun set(index: UInt, item: Item) =
        inner.setItem(index.toInt(), (item as BukkitKhsItem).inner)

    override fun remove(item: Item) = inner.remove((item as BukkitKhsItem).inner)

    override var contents: List<Item?>
        get() = inner.contents.map { toKhsItem(it) }
        set(contents: List<Item?>) =
            inner.setContents(contents.map { (it as BukkitKhsItem).inner }.toTypedArray())

    override fun clear() {
        inner.clear()
    }
}

class BukkitKhsPlayerInventory(
    override val shim: BukkitKhsShim,
    override val inner: BukkitPlayerInventory,
    override val title: String?,
) : BukkitKhsInventory(shim, inner, title), KhsPlayerInventory {
    override var helmet: Item?
        get() = toKhsItem(inner.helmet)
        set(item: Item?) {
            inner.helmet = (item as? BukkitKhsItem)?.inner
        }

    override var chestplate: Item?
        get() = toKhsItem(inner.chestplate)
        set(item: Item?) {
            inner.chestplate = (item as? BukkitKhsItem)?.inner
        }

    override var leggings: Item?
        get() = toKhsItem(inner.leggings)
        set(item: Item?) {
            inner.leggings = (item as? BukkitKhsItem)?.inner
        }

    override var boots: Item?
        get() = toKhsItem(inner.boots)
        set(item: Item?) {
            inner.boots = (item as? BukkitKhsItem)?.inner
        }
}
