package cat.freya.khs.command.util

import cat.freya.khs.Khs
import cat.freya.khs.world.Player

interface CommandPart {
    val label: String
}

interface Command : CommandPart {
    val usage: List<String>
    val description: String

    fun execute(plugin: Khs, player: Player, args: List<String>)

    fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String>
}
