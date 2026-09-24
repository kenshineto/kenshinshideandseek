package cat.freya.khs.mod

import cat.freya.khs.game.Board
import java.util.Optional
import java.util.UUID
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.ScoreHolder
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team

class ModTeam(val inner: PlayerTeam) : Board.Team {
    override fun setPrefix(prefix: String) {
        inner.setPlayerPrefix(KhsMod.parseText(prefix))
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

class ModBoard(val mod: KhsMod, val board: Scoreboard, val objective: Objective?) : Board {
    private var blanks: Int = 0

    override fun getTeam(name: String): ModTeam {
        val team = board.getPlayerTeam(name) ?: board.addPlayerTeam(name)
        return ModTeam(team)
    }

    private fun clearObjective() {
        val objective = this.objective ?: return
        for (score in board.listPlayerScores(objective)) {
            val holder = ScoreHolder.forNameOnly(score.owner())
            board.resetSinglePlayerScore(holder, objective)
        }
    }

    private fun addLine(i: Int, line: String) {
        val objective = this.objective ?: return
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
        objective?.displayName = KhsMod.parseText(title)

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

    fun sendTo(player: ServerPlayer) {
        val objective = this.objective

        if (objective != null) {
            // clear the objective on the client
            player.connection.send(ClientboundSetObjectivePacket(objective, 1)) // remove
            player.connection.send(ClientboundSetObjectivePacket(objective, 0)) // create
        }

        // make the objective appear on the sidebar
        player.connection.send(ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, objective))

        if (objective == null) {
            // nothing else to update
            return
        }

        for (score in board.listPlayerScores(objective)) {
            val line = score.owner()
            val display = KhsMod.parseText(line)

            player.connection.send(
                ClientboundSetScorePacket(
                    score.owner(),
                    objective.name,
                    score.value(),
                    Optional.of(display),
                    Optional.empty(),
                )
            )
        }
    }
}
