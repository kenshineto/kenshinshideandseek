package cat.freya.khs

import cat.freya.khs.db.PlayerStat
import cat.freya.khs.game.Game
import cat.freya.khs.world.Player
import java.util.UUID
import kotlin.text.toULong

data class PlaceholderRequest(val plugin: Khs, val uuid: UUID, val placeholder: String) {
    val args = placeholder.split('_')
    val arg0 = args.firstOrNull()

    val invalid = plugin.locale.placeholder.invalid
    val noData = plugin.locale.placeholder.noData
}

private fun handlePlayerRanking(req: PlaceholderRequest): String {
    val stat = req.args.getOrNull(1)?.let(PlayerStat::fromArg) ?: return req.invalid
    val target = req.args.getOrNull(2)
    val db = req.plugin.database ?: return req.invalid

    val asULong = runCatching { target?.toULong() }.getOrElse { null }
    val asUUID = runCatching { UUID.fromString(target) }.getOrElse { null }

    if (asULong != null) {
        val player = db.getByNthStat(asULong - 1UL, stat)
        return player?.name ?: req.noData
    }

    val rank =
        when {
            asUUID != null -> db.getPlayerStatRank(asUUID, stat)
            target != null -> db.getPlayerStatRank(target, stat)
            else -> db.getPlayerStatRank(req.uuid, stat)
        }

    return rank?.toString() ?: req.noData
}

private fun handlePlayerStat(req: PlaceholderRequest): String {
    val stat = req.args.getOrNull(1)?.let(PlayerStat::fromArg) ?: return req.invalid
    val target = req.args.getOrNull(2)
    val db = req.plugin.database ?: return req.invalid

    val asULong = runCatching { target?.toULong() }.getOrElse { null }
    val asUUID = runCatching { UUID.fromString(target) }.getOrElse { null }

    val player =
        when {
            asULong != null && asULong > 0UL -> db.getByNthStat(asULong - 1UL, stat)
            asUUID != null -> db.getPlayer(asUUID)
            target != null -> db.getPlayer(target)
            else -> db.getPlayer(req.uuid)
        }

    if (player == null) return req.noData

    return stat.getValue(player).toString()
}

private fun handleLastGame(req: PlaceholderRequest): String {
    val arg1 = req.args.getOrNull(1)
    val arg2 = req.args.getOrNull(2)
    val arg3 = req.args.getOrNull(3)

    val hasWon =
        when (arg1) {
            "win" -> true
            "loose" -> false
            else -> return req.invalid
        }

    val teamFilter =
        when (arg2) {
            "hider" -> Game.Team.HIDER
            "seeker" -> Game.Team.SEEKER
            else -> null
        }

    if (teamFilter == null && arg3 != null) {
        // arg2 must be a team filter and its invalid!
        return req.invalid
    }

    val indexArg =
        if (teamFilter == null) {
            arg2
        } else {
            arg3
        }
    val index = indexArg?.toUIntOrNull()

    val game = req.plugin.game
    val map =
        if (hasWon) {
            game.getLastWinners()
        } else {
            game.getLastLoosers()
        }

    val players = map.mapNotNull { entry ->
        val player = req.plugin.shim.getPlayer(entry.key) ?: return@mapNotNull null
        if (teamFilter != null && entry.value != teamFilter) return@mapNotNull null
        player
    }

    if (players.isEmpty()) {
        return req.noData
    }

    if (index != null) {
        // display a given winner
        return players.getOrNull(index.toInt())?.let(Player::name) ?: req.noData
    } else {
        // display all winners
        return players.map(Player::name).joinToString(" ")
    }
}

fun handlePlaceholder(req: PlaceholderRequest): String {
    val arg0 = req.arg0 ?: return req.invalid
    return when (arg0) {
        // game info
        "hiders" -> {
            req.plugin.game.teams.hiderCount().toString()
        }

        "seekers" -> {
            req.plugin.game.teams.seekerCount().toString()
        }

        "spectators" -> {
            req.plugin.game.teams.spectatorCount().toString()
        }

        "map" -> {
            req.plugin.game.map?.name ?: req.noData
        }

        // player team
        "team" -> {
            req.plugin.game.teams.get(req.uuid)?.toString() ?: req.noData
        }

        // database
        "rank" -> {
            handlePlayerRanking(req)
        }

        "stat" -> {
            handlePlayerStat(req)
        }

        // last game
        "last" -> {
            handleLastGame(req)
        }

        // else
        else -> {
            req.invalid
        }
    }
}
