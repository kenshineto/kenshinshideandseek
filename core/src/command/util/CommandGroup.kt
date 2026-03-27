package cat.freya.khs.command.util

import cat.freya.khs.Khs
import cat.freya.khs.player.Player

private data class CommandData(val command: Command, val permission: String, val args: List<String>)

class CommandGroup(val plugin: Khs, override val label: String, vararg commands: CommandPart) :
    CommandPart {
    // set of commands to run in this group
    private final val REGISTRY: Map<String, CommandPart> =
        commands.associate { it.label.lowercase() to it }

    private fun getCommand(args: List<String>, permission: String): CommandData? {
        val invoke = args.firstOrNull()?.lowercase() ?: return null
        val command = REGISTRY.get(invoke) ?: return null

        return when (command) {
            is Command -> CommandData(command, "$permission.$invoke", args.drop(1))
            is CommandGroup -> command.getCommand(args.drop(1), "$permission.$invoke")
            else -> null
        }
    }

    private fun messageAbout(player: Player) {
        val version = plugin.shim.pluginVersion
        player.message(
            "&b&lKenshin's Hide and Seek &7(&f$version&7)\n" +
                "&7Author: &f[KenshinEto]\n" +
                "&7Help Command: &b/hs &fhelp"
        )
    }

    fun handleCommand(player: Player, args: List<String>) {
        val data = getCommand(args, label) ?: return messageAbout(player)

        if (plugin.saving) {
            player.message(plugin.locale.prefix.error + plugin.locale.command.notAllowedTemp)
            return
        }

        if (plugin.config.permissionsRequired && !player.hasPermission(data.permission)) {
            player.message(plugin.locale.prefix.error + plugin.locale.command.notAllowed)
            return
        }

        val paramCount = data.command.usage.filter { it.firstOrNull() != '*' }.count()
        if (data.args.size < paramCount) {
            player.message(plugin.locale.prefix.error + plugin.locale.command.notEnoughArguments)
            return
        }

        runCatching { data.command.execute(plugin, player, data.args) }
            .onFailure {
                player.message(
                    plugin.locale.prefix.error + (it.message ?: plugin.locale.command.unknownError)
                )

                if (plugin.config.debug) {
                    plugin.shim.logger.warning("=== KHS BEGIN DEBUG TRACE ===")
                    plugin.shim.logger.warning(it.stackTraceToString())
                    plugin.shim.logger.warning("=== KHS END DEBUG TRACE ===")
                }
            }
    }

    private fun handleTabComplete(
        player: Player,
        args: List<String>,
        permission: String,
    ): List<String> {
        val invoke = args.firstOrNull()?.lowercase() ?: return listOf()
        val command = REGISTRY.get(invoke)
        return when {
            command is Command -> {
                if (
                    plugin.config.permissionsRequired &&
                        !player.hasPermission("$permission.$invoke")
                )
                    return listOf()

                var index = maxOf(args.size - 1, 1)
                val typed = args.getOrNull(index) ?: return listOf()

                // handle last argument of usage being a varadic (...)
                if (
                    index >= command.usage.size &&
                        command.usage.lastOrNull()?.endsWith("...") == true
                )
                    index = command.usage.size

                val parameter = command.usage.getOrNull(index - 1) ?: return listOf()

                command.autoComplete(plugin, parameter, typed)
            }
            command is CommandGroup ->
                command.handleTabComplete(player, args.drop(1), "$permission.$invoke")
            args.size == 1 -> REGISTRY.keys.filter { it.startsWith(invoke) }
            else -> listOf()
        }
    }

    fun handleTabComplete(player: Player, args: List<String>): List<String> {
        return handleTabComplete(player, args, label)
    }

    private fun commandsFor(
        player: Player,
        label: String,
        permission: String,
        res: MutableList<Pair<String, Command>>,
    ) {
        for ((invoke, command) in REGISTRY) {
            when (command) {
                is Command -> {
                    if (
                        plugin.config.permissionsRequired &&
                            !player.hasPermission("$permission.$invoke")
                    )
                        continue
                    res.add("$label $invoke" to command)
                }
                is CommandGroup ->
                    command.commandsFor(player, "$label $invoke", "$permission.$invoke", res)
            }
        }
    }

    fun commandsFor(player: Player): List<Pair<String, Command>> {
        val commands = mutableListOf<Pair<String, Command>>()
        commandsFor(player, label, label, commands)
        return commands
    }
}
