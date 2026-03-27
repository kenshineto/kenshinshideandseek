package cat.freya.khs.db

import java.util.UUID
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
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
    this[Players.uuid] = player.uuid.toString()
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
