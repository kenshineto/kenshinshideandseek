package cat.freya.khs.db

import java.util.UUID
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder

object Players : Table("hs_players") {
    val uuid = varchar("uuid", 36)
    val name = text("name").nullable()
    val seekerWins = integer("seeker_wins").default(0)
    val hiderWins = integer("hider_wins").default(0)
    val seekerLosses = integer("seeker_losses").default(0)
    val hiderLosses = integer("hider_losses").default(0)
    val seekerKills = integer("seeker_kills").default(0)
    val hiderKills = integer("hider_kills").default(0)
    val seekerDeaths = integer("seeker_deaths").default(0)
    val hiderDeaths = integer("hider_deaths").default(0)

    override val primaryKey = PrimaryKey(uuid)
}

data class Player(
    val uuid: UUID,
    var name: String? = null,
    var seekerWins: UInt = 0u,
    var hiderWins: UInt = 0u,
    var seekerLosses: UInt = 0u,
    var hiderLosses: UInt = 0u,
    var seekerKills: UInt = 0u,
    var hiderKills: UInt = 0u,
    var seekerDeaths: UInt = 0u,
    var hiderDeaths: UInt = 0u,
)

fun ResultRow.toPlayer(): Player {
    return Player(
        uuid = UUID.fromString(this[Players.uuid]),
        name = this[Players.name],
        seekerWins = this[Players.seekerWins].toUInt(),
        hiderWins = this[Players.hiderWins].toUInt(),
        seekerLosses = this[Players.seekerLosses].toUInt(),
        hiderLosses = this[Players.hiderLosses].toUInt(),
        seekerKills = this[Players.seekerKills].toUInt(),
        hiderKills = this[Players.hiderKills].toUInt(),
        seekerDeaths = this[Players.seekerDeaths].toUInt(),
        hiderDeaths = this[Players.hiderDeaths].toUInt(),
    )
}

fun UpdateBuilder<*>.fromPlayer(player: Player) {
    // this[Players.uuid] = player.uuid.toString()
    this[Players.name] = player.name
    this[Players.seekerWins] = player.seekerWins.toInt()
    this[Players.hiderWins] = player.hiderWins.toInt()
    this[Players.seekerLosses] = player.seekerLosses.toInt()
    this[Players.hiderLosses] = player.hiderLosses.toInt()
    this[Players.seekerKills] = player.seekerKills.toInt()
    this[Players.hiderKills] = player.hiderKills.toInt()
    this[Players.seekerDeaths] = player.seekerDeaths.toInt()
    this[Players.hiderDeaths] = player.hiderDeaths.toInt()
}

enum class PlayerStat(val arg: String) {
    TOTAL_WINS("wins"),
    HIDER_WINS("hiderWins"),
    SEEKER_WINS("seekerWins"),
    TOTAL_LOSSES("losses"),
    HIDER_LOSSES("hiderLosses"),
    SEEKER_LOSSES("seekerLosses"),
    TOTAL_GAMES("games"),
    HIDER_GAMES("hiderGames"),
    SEEKER_GAMES("seekerGames"),
    TOTAL_KILLS("kills"),
    HIDER_KILLS("hiderKills"),
    SEEKER_KILLS("seekerKills"),
    TOTAL_DEATHS("deaths"),
    HIDER_DEATHS("hiderDeaths"),
    SEEKER_DEATHS("seekerDeaths");

    companion object {
        fun fromArg(arg: String): PlayerStat? =
            PlayerStat.entries.firstOrNull { it.arg.equals(arg, ignoreCase = true) }
    }

    fun getValue(player: Player): UInt =
        when (this) {
            TOTAL_WINS -> player.hiderWins + player.seekerWins
            HIDER_WINS -> player.hiderWins
            SEEKER_WINS -> player.seekerWins
            TOTAL_LOSSES -> player.hiderLosses + player.seekerLosses
            HIDER_LOSSES -> player.hiderLosses
            SEEKER_LOSSES -> player.seekerLosses
            TOTAL_GAMES ->
                player.hiderWins + player.seekerWins + player.hiderLosses + player.seekerLosses
            HIDER_GAMES -> player.hiderWins + player.hiderLosses
            SEEKER_GAMES -> player.seekerWins + player.seekerLosses
            TOTAL_KILLS -> player.hiderKills + player.seekerKills
            HIDER_KILLS -> player.hiderKills
            SEEKER_KILLS -> player.seekerKills
            TOTAL_DEATHS -> player.hiderDeaths + player.seekerDeaths
            HIDER_DEATHS -> player.hiderDeaths
            SEEKER_DEATHS -> player.seekerDeaths
        }

    fun getExpr(): Expression<Int> =
        when (this) {
            TOTAL_WINS -> Players.hiderWins + Players.seekerWins
            HIDER_WINS -> Players.hiderWins
            SEEKER_WINS -> Players.seekerWins
            TOTAL_LOSSES -> Players.hiderLosses + Players.seekerLosses
            HIDER_LOSSES -> Players.hiderLosses
            SEEKER_LOSSES -> Players.seekerLosses
            TOTAL_GAMES ->
                Players.hiderWins + Players.seekerWins + Players.hiderLosses + Players.seekerLosses
            HIDER_GAMES -> Players.hiderWins + Players.hiderLosses
            SEEKER_GAMES -> Players.seekerWins + Players.seekerLosses
            TOTAL_KILLS -> Players.hiderKills + Players.seekerKills
            HIDER_KILLS -> Players.hiderKills
            SEEKER_KILLS -> Players.seekerKills
            TOTAL_DEATHS -> Players.hiderDeaths + Players.seekerDeaths
            HIDER_DEATHS -> Players.hiderDeaths
            SEEKER_DEATHS -> Players.seekerDeaths
        }
}
