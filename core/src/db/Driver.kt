package cat.freya.khs.db

import cat.freya.khs.Khs
import cat.freya.khs.config.DatabaseConfig
import cat.freya.khs.config.DatabaseType
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

abstract class Driver {
    abstract val driverClass: String

    abstract fun jdbcUrl(): String

    abstract fun configure(hikari: HikariConfig)

    fun connect(): DataSource {
        // load driver for some reason
        Class.forName(driverClass)

        val cores = Runtime.getRuntime().availableProcessors()
        val hikari =
            HikariConfig().apply {
                jdbcUrl = jdbcUrl()
                driverClassName = driverClass
                maximumPoolSize = minOf(cores, 8)
                configure(this)
            }
        return HikariDataSource(hikari)
    }
}

class SqliteDriver(val path: String) : Driver() {
    override val driverClass = "org.sqlite.JDBC"

    override fun jdbcUrl(): String {
        return "jdbc:sqlite:$path"
    }

    override fun configure(hikari: HikariConfig) {
        // sqlite is single threaded
        hikari.maximumPoolSize = 1
    }
}

class MysqlDriver(val config: DatabaseConfig) : Driver() {
    override val driverClass = "com.mysql.cj.jdbc.Driver"

    override fun jdbcUrl(): String {
        val port = config.port ?: 3006u
        return "jdbc:mysql://${config.host}:${port}/${config.database}"
    }

    override fun configure(hikari: HikariConfig) {
        hikari.username = config.username
        hikari.password = config.password
    }
}

class PostgresDriver(val config: DatabaseConfig) : Driver() {
    override val driverClass = "org.postgresql.Driver"

    override fun jdbcUrl(): String {
        val port = config.port ?: 5432u
        return "jdbc:postgresql://${config.host}:${port}/${config.database}"
    }

    override fun configure(hikari: HikariConfig) {
        hikari.username = config.username
        hikari.password = config.password
    }
}

fun getDriver(plugin: Khs): Driver =
    when (plugin.config.database.type) {
        DatabaseType.SQLITE -> SqliteDriver(plugin.shim.sqliteDatabasePath)
        DatabaseType.MYSQL -> MysqlDriver(plugin.config.database)
        DatabaseType.POSTGRES -> PostgresDriver(plugin.config.database)
    }
