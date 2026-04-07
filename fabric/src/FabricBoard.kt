package cat.freya.khs.fabric

import cat.freya.khs.game.Board
import java.util.UUID
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.ScoreHolder
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team

class FabricTeam(val inner: PlayerTeam) : Board.Team {

    override fun setPrefix(prefix: String) {
        inner.setPlayerPrefix(Component.literal(prefix))
    }

    override fun setCanCollide(canCollide: Boolean) {
        inner.collisionRule =
            if (canCollide) {
                Team.CollisionRule.ALWAYS
            } else {
                Team.CollisionRule.NEVER
            }
    }

    override fun setNameTagsVisible(nameTagsVisible: Boolean) {
        inner.nameTagVisibility =
            if (nameTagsVisible) {
                Team.Visibility.HIDE_FOR_OTHER_TEAMS
            } else {
                Team.Visibility.NEVER
            }
    }

    override fun setPlayers(players: Set<UUID>) {
        inner.players.clear()
        for (uuid in players) {
            inner.players.add(uuid.toString())
        }
    }
}

class FabricBoard(val board: Scoreboard, val objective: Objective?) : Board {

    private var blanks: Int = 0

    override fun getTeam(name: String): FabricTeam {
        val team = board.getPlayerTeam(name) ?: board.addPlayerTeam(name)
        return FabricTeam(team)
    }

    private fun clearObjective() {
        val objective = objective ?: return
        for (score in board.listPlayerScores(objective)) {
            val holder = ScoreHolder.forNameOnly(score.owner())
            board.resetSinglePlayerScore(holder, objective)
        }
    }

    private fun addLine(i: Int, line: String) {
        val objective = objective ?: return
        val holder = ScoreHolder.forNameOnly(line)
        val score = board.getOrCreatePlayerScore(holder, objective)
        score.set(i + 1)
    }

    private fun addBlank(i: Int) {
        blanks++
        addLine(i, " ".repeat(blanks))
    }

    override fun setText(title: String, text: List<String>) {
        clearObjective()

        // set title
        objective?.displayName = Component.literal(title)

        // set content
        blanks = 0
        for ((i, line) in text.withIndex()) {
            if (line.trim().isEmpty()) {
                addBlank(i)
                continue
            }

            addLine(i, line)
        }
    }
}
