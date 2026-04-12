package cat.freya.khs.db

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import java.nio.ByteBuffer
import java.util.UUID

// tables introduced in version 1.7.0
// pre 1.7.x tables are NOT SUPPORTED

object LegacyNames : Table("hs_names") {
    val uuid = binary("uuid", 16)
    val name = varchar("name", 48).nullable()
    override val primaryKey = PrimaryKey(uuid, name)
}

object LegacyPlayers : Table("hs_data") {
    val uuid = binary("uuid", 16)
    val hiderWins = integer("hider_wins").nullable()
    val seekerWins = integer("seeker_wins").nullable()
    val hiderGames = integer("hider_games").nullable()
    val seekerGames = integer("seeker_games").nullable()
    val hiderKills = integer("hider_kills").nullable()
    val seekerKills = integer("seeker_kills").nullable()
    val hiderDeaths = integer("hider_deaths").nullable()
    val seekerDeaths = integer("seeker_deaths").nullable()
}

fun ResultRow.toLegacyPlayer(): Player {
    val uuidBuffer = ByteBuffer.wrap(this[LegacyPlayers.uuid])
    val uuidHigh = uuidBuffer.long
    val uuidLow = uuidBuffer.long
    val uuid = UUID(uuidHigh, uuidLow)

    val hiderGames = this[LegacyPlayers.hiderGames] ?: 0
    val seekerGames = this[LegacyPlayers.seekerGames] ?: 0
    val hiderWins = this[LegacyPlayers.hiderWins] ?: 0
    val seekerWins = this[LegacyPlayers.seekerWins] ?: 0
    val hiderLosses = hiderGames - hiderWins
    val seekerLosses = seekerGames - seekerWins
    val hiderKills = this[LegacyPlayers.hiderKills] ?: 0
    val seekerKills = this[LegacyPlayers.seekerKills] ?: 0
    val hiderDeaths = this[LegacyPlayers.hiderDeaths] ?: 0
    val seekerDeaths = this[LegacyPlayers.seekerDeaths] ?: 0

    return Player(
        uuid,
        name = this[LegacyNames.name],
        seekerWins = maxOf(seekerWins, 0).toUInt(),
        hiderWins = maxOf(hiderWins, 0).toUInt(),
        hiderLosses = maxOf(hiderLosses, 0).toUInt(),
        seekerLosses = maxOf(seekerLosses, 0).toUInt(),
        seekerKills = maxOf(seekerKills, 0).toUInt(),
        hiderKills = maxOf(hiderKills, 0).toUInt(),
        seekerDeaths = maxOf(seekerDeaths, 0).toUInt(),
        hiderDeaths = maxOf(hiderDeaths, 0).toUInt(),
    )
}
