package cat.freya.khs.mod.event

import cat.freya.khs.event.DropEvent
import cat.freya.khs.event.HungerEvent
import cat.freya.khs.event.RegenEvent
import cat.freya.khs.event.onDrop
import cat.freya.khs.event.onHunger
import cat.freya.khs.event.onRegen
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.ModItem
import cat.freya.khs.mod.ModPlayer
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.event.events.common.TickEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.item.ItemEntity
import java.util.UUID

class PlayerListener(val mod: KhsMod) {
    private val savedPrevHealth: MutableMap<UUID, Double> = mutableMapOf()
    private val savedPrevHunger: MutableMap<UUID, UInt> = mutableMapOf()

    init {
        TickEvent.SERVER_POST.register { _ ->
            mod.server.getPlayers().forEach { handlePlayer(it) }
        }

        PlayerEvent.DROP_ITEM.register { player, itemEntity ->
            handleDrop(player as ServerPlayer, itemEntity)
        }
    }

    private fun handlePlayer(player: ModPlayer) {
        val health = player.getHealth()
        val hunger = player.getHunger()

        val prevHealth = savedPrevHealth[player.uuid] ?: health
        val prevHunger = savedPrevHunger[player.uuid] ?: hunger

        // handle regen
        if (health > prevHealth) {
            val diff = health - prevHealth
            val natural = diff <= 1

            val khsEvent = RegenEvent(mod.khs, player, natural)
            onRegen(khsEvent)

            if (khsEvent.cancelled) {
                player.setHealth(prevHealth)
            }
        }

        // handle hunger
        if (prevHunger < hunger) {
            val khsEvent = HungerEvent(mod.khs, player)
            onHunger(khsEvent)

            if (khsEvent.cancelled) {
                player.setHunger(prevHunger)
            }
        }

        savedPrevHealth[player.uuid] = health
        savedPrevHunger[player.uuid] = hunger
    }

    private fun handleDrop(player: ServerPlayer, itemEntity: ItemEntity): EventResult {
        val item = ModItem.wrap(itemEntity.item) ?: return EventResult.pass()

        val khsPlayer = ModPlayer(mod, player)
        val khsEvent = DropEvent(mod.khs, khsPlayer, item)
        onDrop(khsEvent)

        return eventResult(khsEvent)
    }
}
