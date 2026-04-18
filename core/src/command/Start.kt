package cat.freya.khs.command

import cat.freya.khs.Khs
import cat.freya.khs.command.util.Command
import cat.freya.khs.runChecks
import cat.freya.khs.world.Player

class KhsStart : Command {
    override val label = "start"
    override val usage = listOf("*seekers...")
    override val description =
        "Starts the game either with a random set of seekers or a chosen list"

    override fun execute(plugin: Khs, player: Player, args: List<String>) {
        runChecks(plugin, player) {
            gameMapExists()
            gameNotInProgress()
            playerInGame()
            lobbyHasEnoughPlayers()
        }

        val pool =
            args.mapNotNull { plugin.shim.getPlayer(it)?.uuid }.filter { plugin.game.teams.contains(it) }

        plugin.game.start(pool)
    }

    override fun autoComplete(plugin: Khs, parameter: String, typed: String): List<String> {
        return plugin.game.teams
            .getPlayers()
            .map(Player::name)
    }
}
