package cat.freya.khs.fabric

import cat.freya.khs.fabric.event.*
import cat.freya.khs.mod.KhsMod
import cat.freya.khs.mod.internal.CommandManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

object KhsModFabric : ModInitializer {
    lateinit var mod: KhsMod

    override fun onInitialize() {
        mod = KhsMod("Fabric")

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            CommandManager(mod, dispatcher)
        }

        ServerTickEvents.END_SERVER_TICK.register { _ ->
            mod.doTick()
        }

        ServerLifecycleEvents.SERVER_STOPPING.register { _ ->
            mod.onShutdown()
        }

        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            mod.init(server)
            registerListeners()
        }
    }

    private fun registerListeners() {
        BreakListener(mod)
        ChatListener(mod)
        DamageListener(mod)
        InteractListener(mod)
        JoinLeaveListener(mod)
        MovementListener(mod)
        PlayerListener(mod)
        RespawnListener(mod)
    }
}
