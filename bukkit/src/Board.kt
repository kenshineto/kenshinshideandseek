@file:Suppress("DEPRECATION")

package cat.freya.khs.bukkit

import cat.freya.khs.game.Board as KhsBoard
import java.util.UUID
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.NameTagVisibility
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard as BukkitBoard
import org.bukkit.scoreboard.Team as BukkitTeam

class BukkitKhsTeam(val shim: BukkitKhsShim, private val inner: BukkitTeam) : KhsBoard.Team {
    override var prefix: String
        get() = inner.prefix
        set(prefix) {
            inner.prefix = formatText(prefix)
        }

    // options
    override var canCollide: Boolean
        get() {
            if (!shim.supports(9)) return false
            return inner.getOption(BukkitTeam.Option.COLLISION_RULE) ==
                BukkitTeam.OptionStatus.NEVER
        }
        set(b) {
            if (shim.supports(9)) {
                val v = if (b) BukkitTeam.OptionStatus.ALWAYS else BukkitTeam.OptionStatus.NEVER
                inner.setOption(BukkitTeam.Option.COLLISION_RULE, v)
            }
        }

    override var nameTagsVisible: Boolean
        get() {
            return if (shim.supports(9)) {
                inner.getOption(BukkitTeam.Option.NAME_TAG_VISIBILITY) !=
                    BukkitTeam.OptionStatus.NEVER
            } else {
                inner.nameTagVisibility != NameTagVisibility.NEVER
            }
        }
        set(b) {
            if (shim.supports(9)) {
                val v =
                    if (b) BukkitTeam.OptionStatus.FOR_OWN_TEAM else BukkitTeam.OptionStatus.NEVER
                inner.setOption(BukkitTeam.Option.NAME_TAG_VISIBILITY, v)
            } else {
                val v = if (b) NameTagVisibility.HIDE_FOR_OTHER_TEAMS else NameTagVisibility.NEVER
                inner.nameTagVisibility = v
            }
        }

    // players
    override var players: Set<UUID>
        get() = inner.entries.mapNotNull { shim.getPlayer(it)?.uuid }.toSet()
        set(new) {
            for (entry in inner.entries) {
                val player = shim.plugin.server.getPlayer(entry)
                if (!new.contains(player?.uniqueId)) inner.removeEntry(entry)
            }
            for (uuid in new) {
                val player = shim.plugin.server.getPlayer(uuid) ?: continue
                inner.addEntry(player.name)
            }
        }
}

class BukkitKhsBoard(val shim: BukkitKhsShim, private val inner: BukkitBoard) : KhsBoard {
    private var objective: Objective? = null
    private var blanks: Int = 0

    private fun resetObjective() {
        objective =
            if (shim.supports(13)) {
                inner.registerNewObjective("Scoreboard", "dummy", "")
            } else {
                inner.registerNewObjective("Scoreboard", "dummy")
            }
        blanks = 0
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

    override fun getTeam(name: String): KhsBoard.Team {
        runCatching { inner.registerNewTeam(name) }
        val team = inner.getTeam(name) ?: error("failed to make team ?!?")
        return BukkitKhsTeam(shim, team)
    }

    override fun display(uuid: UUID) {
        val player = shim.getPlayer(uuid) ?: return
        objective?.displaySlot = DisplaySlot.SIDEBAR
        (player as BukkitKhsPlayer).inner.scoreboard = inner
    }
}
