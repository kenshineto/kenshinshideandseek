package cat.freya.khs.fabric

import cat.freya.khs.player.Inventory as KhsInventory
import cat.freya.khs.player.PlayerInventory as KhsPlayerInventory
import cat.freya.khs.world.Item as KhsItem
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack

class FabricContainer(val size: UInt, val title: String) : Container {
    private val items = MutableList(size.toInt()) { ItemStack.EMPTY }

    override fun clearContent() {
        items.fill(ItemStack.EMPTY)
    }

    override fun getContainerSize(): Int {
        return items.size
    }

    override fun isEmpty(): Boolean {
        return items.any { !it.isEmpty() }
    }

    override fun getItem(slot: Int): ItemStack {
        return items.get(slot)
    }

    override fun removeItem(slot: Int, count: Int): ItemStack {
        val item = getItem(slot)
        if (item == ItemStack.EMPTY) return item

        if (item.count <= count) {
            setItem(slot, ItemStack.EMPTY)
            return item
        } else {
            val res = item.copy()
            item.shrink(count)
            return res
        }
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        return removeItem(slot, 1)
    }

    override fun setItem(slot: Int, itemStack: ItemStack) {
        items.set(slot, itemStack)
    }

    override fun setChanged() {
        /* eh */
    }

    override fun stillValid(player: Player): Boolean {
        return true
    }
}

open class FabricKhsInventory(open val shim: FabricKhsShim, val container: Container) :
    KhsInventory {
    override val title: String?
        get() =
            when (container) {
                is FabricContainer -> container.title
                is Inventory -> container.displayName.string
                else -> null
            }

    private fun range(): IntRange {
        return 0 until container.containerSize
    }

    fun getMenuType(): MenuType<ChestMenu> {
        return when (container.containerSize) {
            9 -> MenuType.GENERIC_9x1
            18 -> MenuType.GENERIC_9x2
            27 -> MenuType.GENERIC_9x3
            36 -> MenuType.GENERIC_9x4
            45 -> MenuType.GENERIC_9x5
            54 -> MenuType.GENERIC_9x6
            else -> error("unsupported inventory size")
        }
    }

    fun createMenu(player: ServerPlayer): ChestMenu {
        return ChestMenu(
            getMenuType(),
            -1,
            player.inventory,
            container,
            container.containerSize / 9,
        )
    }

    override fun get(index: UInt): KhsItem? {
        val item = runCatching { container.getItem(index.toInt()) }.getOrElse { null }
        return toKhsItem(item)
    }

    override fun set(index: UInt, item: KhsItem) {
        val stack = (item as? FabricKhsItem)?.inner ?: return
        runCatching { container.setItem(index.toInt(), stack) }
    }

    override fun remove(item: KhsItem) {
        val stack = (item as? FabricKhsItem)?.inner ?: return
        for (i in range()) {
            val value = container.getItem(i)
            if (value.equals(stack)) container.setItem(i, ItemStack.EMPTY)
        }
    }

    override var contents: List<KhsItem?>
        get() = range().map { toKhsItem(container.getItem(it)) }
        set(contents) {
            for ((i, item) in contents.withIndex()) {
                if (i >= container.containerSize) break
                val stack = (item as? FabricKhsItem)?.inner ?: ItemStack.EMPTY
                container.setItem(i, stack)
            }
        }

    override fun clear() {
        container.clearContent()
    }
}

class FabricKhsPlayerInventory(
    override val shim: FabricKhsShim,
    val inner: Inventory,
    val player: ServerPlayer,
) : FabricKhsInventory(shim, inner), KhsPlayerInventory {
    override var helmet: KhsItem?
        get() = toKhsItem(player.getItemBySlot(EquipmentSlot.HEAD))
        set(item) {
            val stack = (item as? FabricKhsItem)?.inner ?: return
            player.setItemSlot(EquipmentSlot.HEAD, stack)
        }

    override var chestplate: KhsItem?
        get() = toKhsItem(player.getItemBySlot(EquipmentSlot.CHEST))
        set(item) {
            val stack = (item as? FabricKhsItem)?.inner ?: return
            player.setItemSlot(EquipmentSlot.CHEST, stack)
        }

    override var leggings: KhsItem?
        get() = toKhsItem(player.getItemBySlot(EquipmentSlot.LEGS))
        set(item) {
            val stack = (item as? FabricKhsItem)?.inner ?: return
            player.setItemSlot(EquipmentSlot.LEGS, stack)
        }

    override var boots: KhsItem?
        get() = toKhsItem(player.getItemBySlot(EquipmentSlot.FEET))
        set(item) {
            val stack = (item as? FabricKhsItem)?.inner ?: return
            player.setItemSlot(EquipmentSlot.FEET, stack)
        }

    override fun clear() {
        inner.clearContent()
        helmet = null
        chestplate = null
        leggings = null
        boots = null
    }
}

fun toKhsItem(inner: ItemStack?): FabricKhsItem? {
    if (inner == null) return null
}
