package cat.freya.khs

import cat.freya.khs.db.PlayerStat
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

fun handlePlaceholder(req: PlaceholderRequest): String {
    val arg0 = req.arg0 ?: return req.invalid
    return when (arg0) {
        // game info
        "hiders" -> {
            req.plugin.game.teams
                .hiderCount()
                .toString()
        }

        "seekers" -> {
            req.plugin.game.teams
                .seekerCount()
                .toString()
        }

        "spectators" -> {
            req.plugin.game.teams
                .spectatorCount()
                .toString()
        }

        "map" -> {
            req.plugin.game.map
                ?.name ?: req.noData
        }

        // player team
        "team" -> {
            req.plugin.game
                .teams
                .get(req.uuid)
                ?.toString() ?: req.noData
        }

        // database
        "rank" -> {
            handlePlayerRanking(req)
        }

        "stat" -> {
            handlePlayerStat(req)
        }

        // else
        else -> {
            req.invalid
        }
    }
}
