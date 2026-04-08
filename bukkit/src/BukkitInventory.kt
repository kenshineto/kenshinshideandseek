package cat.freya.khs.bukkit

import cat.freya.khs.world.Inventory
import cat.freya.khs.world.Item
import cat.freya.khs.world.PlayerInventory

open class BukkitInventory(
    open val shim: BukkitKhsShim,
    open val inner: org.bukkit.inventory.Inventory,
    override val title: String?,
) : Inventory {
    override fun get(index: UInt): Item? {
        val item = runCatching { inner.getItem(index.toInt()) }.getOrElse { null }
        return BukkitItem.wrap(item)
    }

    override fun set(index: UInt, item: Item?) {
        val bukkitItem = (item as? BukkitItem)?.inner

        // make sure we don't go out of bounds
        if (index.toInt() >= inner.size) return

        inner.setItem(index.toInt(), bukkitItem)
    }

    override fun remove(item: Item) {
        val bukkitItem = (item as? BukkitItem)?.inner ?: return
        inner.remove(bukkitItem)
    }

    override fun getContents(): List<Item?> {
        return inner.contents.map { BukkitItem.wrap(it) }
    }

    override fun setContents(contents: List<Item?>) {
        inner.contents = contents.map { (it as? BukkitItem)?.inner }.toTypedArray()
    }

    override fun clearContents() {
        inner.clear()
    }
}

class BukkitPlayerInventory(
    override val shim: BukkitKhsShim,
    override val inner: org.bukkit.inventory.PlayerInventory,
    override val title: String?,
) : BukkitInventory(shim, inner, title), PlayerInventory {

    override fun getHelmet(): Item? {
        return BukkitItem.wrap(inner.helmet)
    }

    override fun setHelmet(helmet: Item?) {
        inner.helmet = (helmet as? BukkitItem)?.inner
    }

    override fun getChestplate(): Item? {
        return BukkitItem.wrap(inner.chestplate)
    }

    override fun setChestplate(chestplate: Item?) {
        inner.chestplate = (chestplate as? BukkitItem)?.inner
    }

    override fun getLeggings(): Item? {
        return BukkitItem.wrap(inner.leggings)
    }

    override fun setLeggings(leggings: Item?) {
        inner.leggings = (leggings as? BukkitItem)?.inner
    }

    override fun getBoots(): Item? {
        return BukkitItem.wrap(inner.boots)
    }

    override fun setBoots(boots: Item?) {
        inner.boots = (boots as? BukkitItem)?.inner
    }
}
