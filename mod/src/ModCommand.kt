package cat.freya.khs.mod

import cat.freya.khs.command.util.CommandGroup
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate

class ModCommand<T : CommandSourceStack>(val mod: KhsMod, val command: CommandGroup, val dispatcher: CommandDispatcher<T>) :
    Command<T>,
    Predicate<T>,
    SuggestionProvider<T> {
    init {
        dispatcher.register(
            LiteralArgumentBuilder
                .literal<T>(command.label)
                .executes(this)
                .then(RequiredArgumentBuilder.argument<T, String>("args", StringArgumentType.greedyString()).suggests(this).executes(this)),
        )
    }

    override fun test(source: T): Boolean {
        return source.player != null
    }

    override fun run(ctx: CommandContext<T>): Int {
        val player = ctx.source.player ?: return 0
        val khsPlayer = ModPlayer(mod, player)
        val args = ctx.input.split(" ").drop(1)
        command.handleCommand(khsPlayer, args)
        return 1
    }

    override fun getSuggestions(ctx: CommandContext<T>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val player = ctx.source.player
        if (player == null) {
            return builder.buildFuture()
        }

        val khsPlayer = ModPlayer(mod, player)
        val args = ctx.input.split(" ").drop(1)

        val lastSpace = ctx.input.lastIndexOf(' ')
        val offset = if (lastSpace == -1) 0 else lastSpace + 1
        val withArgsBuilder = builder.createOffset(offset)

        command.handleTabComplete(khsPlayer, args).forEach {
            withArgsBuilder.suggest(it)
        }

        return withArgsBuilder.buildFuture()
    }
}
