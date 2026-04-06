@file:Suppress("DEPRECATION")

package cat.freya.khs.bukkit

import cat.freya.khs.game.Board
import java.util.UUID
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.NameTagVisibility
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard

class BukkitTeam(val shim: BukkitKhsShim, val inner: org.bukkit.scoreboard.Team) : Board.Team {

    override fun setPrefix(prefix: String) {
        inner.prefix = formatText(prefix)
    }

    override fun setCanCollide(canCollide: Boolean) {
        if (!shim.supports(9)) return

        val value =
            if (canCollide) {
                org.bukkit.scoreboard.Team.OptionStatus.ALWAYS
            } else {
                org.bukkit.scoreboard.Team.OptionStatus.NEVER
            }

        inner.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, value)
    }

    override fun setNameTagsVisible(nameTagsVisible: Boolean) {
        if (!shim.supports(9)) {
            // 1.8 does not have team options
            val value =
                if (nameTagsVisible) {
                    NameTagVisibility.HIDE_FOR_OTHER_TEAMS
                } else {
                    NameTagVisibility.NEVER
                }

            inner.nameTagVisibility = value
            return
        }

        val value =
            if (nameTagsVisible) {
                org.bukkit.scoreboard.Team.OptionStatus.FOR_OWN_TEAM
            } else {
                org.bukkit.scoreboard.Team.OptionStatus.NEVER
            }

        inner.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, value)
    }

    override fun setPlayers(players: Set<UUID>) {
        for (player in inner.players) {
            val uuid = player.uniqueId
            if (!players.contains(uuid)) inner.removePlayer(player)
        }

        for (uuid in players) {
            val player = shim.plugin.server.getOfflinePlayer(uuid)
            inner.addPlayer(player)
        }
    }
}

class BukkitBoard(val shim: BukkitKhsShim, val inner: Scoreboard) : Board {
    private var objective: Objective? = null
    private var blanks: Int = 0

    private fun resetObjective() {
        objective = inner.registerNewObjective("Scoreboard", "dummy")

        blanks = 0
        objective?.displaySlot = DisplaySlot.SIDEBAR
    }

    private fun addLine(i: Int, line: String) {
        val score = objective?.getScore(formatText(line))
        score?.score = i + 1
    }

    private fun addBlank(i: Int) {
        blanks++
        addLine(i, " ".repeat(blanks))
    }

    override fun setText(title: String, text: List<String>) {
        resetObjective()

        // set title
        objective?.displayName = formatText(title)

        // set content
        for ((i, line) in text.withIndex()) {
            if (line.trim().isEmpty()) {
                addBlank(i)
                continue
            }

            addLine(i, line)
        }
    }

    override fun getTeam(name: String): Board.Team {
        runCatching { inner.registerNewTeam(name) }
        val team = inner.getTeam(name) ?: error("failed to make team ?!?")
        return BukkitTeam(shim, team)
    }
}
