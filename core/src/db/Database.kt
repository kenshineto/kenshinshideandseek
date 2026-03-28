package cat.freya.khs.db

import cat.freya.khs.Khs
import java.util.UUID
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.Database as Exposed
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class Database(plugin: Khs) {
    val driver = getDriver(plugin)
    val source = driver.connect()
    val db = Exposed.connect(source)

    init {
        transaction(db) { SchemaUtils.create(Players) }
        migrateLegacy()
    }

    fun getPlayer(uuid: UUID): Player? =
        transaction(db) {
            val id = uuid.toString()
            Players.selectAll().where { Players.uuid eq id }.map { it.toPlayer() }.singleOrNull()
        }

    fun getPlayer(name: String): Player? =
        transaction(db) {
            Players.selectAll().where { Players.name eq name }.map { it.toPlayer() }.singleOrNull()
        }

    fun getPlayers(page: UInt, pageSize: UInt): List<Player> =
        transaction(db) {
            val offset = page * pageSize
            val wins = Players.hiderWins + Players.seekerWins
            Players.selectAll()
                .orderBy(wins to SortOrder.DESC)
                .limit(pageSize.toInt())
                .offset(offset.toLong())
                .map { it.toPlayer() }
        }

    fun getPlayerNames(limit: UInt, startsWith: String): List<String> =
        transaction(db) {
            Players.select(Players.name)
                .where { Players.name like "$startsWith%" }
                .orderBy(Players.name to SortOrder.ASC)
                .limit(limit.toInt())
                .map { it[Players.name] }
                .filterNotNull()
        }

    fun upsertPlayer(player: Player) =
        transaction(db) {
            val id = player.uuid.toString()
            val exists = Players.selectAll().where { Players.uuid eq id }.any()

            if (exists) {
                Players.update({ Players.uuid eq id }) { it.fromPlayer(player) }
            } else {
                Players.insert {
                    it[uuid] = id
                    it.fromPlayer(player)
                }
            }
        }

    fun upsertName(u: UUID, n: String) =
        transaction(db) {
            val id = u.toString()

            val current =
                Players.selectAll()
                    .where { Players.uuid eq id }
                    .map { it.toPlayer() }
                    .singleOrNull()

            if (current == null) {
                Players.insert {
                    it[uuid] = id
                    it[name] = n
                }
                return@transaction
            }

            Players.update({ Players.uuid eq id }) { it[name] = n }
        }

    fun migrateLegacy() =
        transaction(db) {
            if (!LegacyPlayers.exists() || !LegacyNames.exists()) return@transaction

            val legacy =
                LegacyPlayers.join(
                        LegacyNames,
                        JoinType.LEFT,
                        onColumn = LegacyPlayers.uuid,
                        otherColumn = LegacyNames.uuid,
                    )
                    .selectAll()
                    .map { it.toLegacyPlayer() }
            Players.insertIgnore { legacy.forEach { player -> it.fromPlayer(player) } }

            SchemaUtils.drop(LegacyPlayers)
            SchemaUtils.drop(LegacyNames)
        }
}
