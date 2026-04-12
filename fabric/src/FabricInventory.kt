package cat.freya.khs.fabric

import cat.freya.khs.type.Item
import cat.freya.khs.world.Inventory
import cat.freya.khs.world.PlayerInventory
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.entity.EquipmentSlot
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
        return items.any { !it.isEmpty }
    }

    override fun getItem(slot: Int): ItemStack {
        return items[slot]
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
        items[slot] = itemStack
    }

    override fun setChanged() {
        // eh
    }

    override fun stillValid(player: Player): Boolean {
        return true
    }
}

open class FabricInventory(open val shim: FabricKhsShim, val container: Container) : Inventory {
    override val title: String?
        get() =
            when (container) {
                is FabricContainer -> container.title
                is net.minecraft.world.entity.player.Inventory -> container.displayName.string
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

    override fun get(index: UInt): Item? {
        val item = runCatching { container.getItem(index.toInt()) }.getOrElse { null }
        return FabricItem.wrap(item)
    }

    override fun set(index: UInt, item: Item?) {
        val stack = (item as? FabricItem)?.inner ?: return
        runCatching { container.setItem(index.toInt(), stack) }
    }

    override fun remove(item: Item) {
        val stack = (item as? FabricItem)?.inner ?: return
        for (i in range()) {
            val value = container.getItem(i)
            if (value == stack) container.setItem(i, ItemStack.EMPTY)
        }
    }

    override fun getContents(): List<Item?> {
        return range().map { container.getItem(it) }.map(FabricItem::wrap)
    }

    override fun setContents(contents: List<Item?>) {
        for ((i, item) in contents.withIndex()) {
            if (i >= container.containerSize) break
            val stack = (item as? FabricItem)?.inner ?: ItemStack.EMPTY
            container.setItem(i, stack)
        }
    }

    override fun clearContents() {
        container.clearContent()
    }
}

class FabricPlayerInventory(override val shim: FabricKhsShim, val player: ServerPlayer) :
    FabricInventory(shim, player.inventory),
    PlayerInventory {
    override fun getHelmet(): Item? {
        return FabricItem.wrap(player.getItemBySlot(EquipmentSlot.HEAD))
    }

    override fun setHelmet(helmet: Item?) {
        val stack = (helmet as? FabricItem)?.inner ?: ItemStack.EMPTY
        player.setItemSlot(EquipmentSlot.HEAD, stack)
    }

    override fun getChestplate(): Item? {
        return FabricItem.wrap(player.getItemBySlot(EquipmentSlot.CHEST))
    }

    override fun setChestplate(chestplate: Item?) {
        val stack = (chestplate as? FabricItem)?.inner ?: ItemStack.EMPTY
        player.setItemSlot(EquipmentSlot.CHEST, stack)
    }

    override fun getLeggings(): Item? {
        return FabricItem.wrap(player.getItemBySlot(EquipmentSlot.LEGS))
    }

    override fun setLeggings(leggings: Item?) {
        val stack = (leggings as? FabricItem)?.inner ?: ItemStack.EMPTY
        player.setItemSlot(EquipmentSlot.LEGS, stack)
    }

    override fun getBoots(): Item? {
        return FabricItem.wrap(player.getItemBySlot(EquipmentSlot.FEET))
    }

    override fun setBoots(boots: Item?) {
        val stack = (boots as? FabricItem)?.inner ?: ItemStack.EMPTY
        player.setItemSlot(EquipmentSlot.FEET, stack)
    }
}
