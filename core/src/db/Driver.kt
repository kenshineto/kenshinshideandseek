package cat.freya.khs.db

import cat.freya.khs.Khs
import cat.freya.khs.config.DatabaseConfig
import cat.freya.khs.config.DatabaseType
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.File
import java.sql.Connection
import javax.sql.DataSource
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource

abstract class Driver {
    abstract fun connect(): DataSource
}

abstract class HikariDriver : Driver() {
    abstract val driverClass: String

    abstract fun jdbcUrl(): String

    abstract fun configure(hikari: HikariConfig)

    override fun connect(): DataSource {
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
    private val driverClass = "org.sqlite.JDBC"
    private val jdbcUrl = "jdbc:sqlite:$path"

    companion object {
        private var memCon: Connection? = null
    }

    override fun connect(): DataSource {
        Class.forName(driverClass)

        val config = SQLiteConfig()
        config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL)
        config.setTempStore(SQLiteConfig.TempStore.MEMORY)

        val source = SQLiteDataSource(config)
        source.url = jdbcUrl

        // keep in memory database alive
        if (path.startsWith("file::memory:") && memCon?.isClosed != false) {
            memCon = source.connection
        }

        return KhsDataSource(source)
    }
}

class MysqlDriver(val config: DatabaseConfig) : HikariDriver() {
    override val driverClass = "com.mysql.cj.jdbc.Driver"

    override fun jdbcUrl(): String {
        val port = config.port ?: 3006u
        return "jdbc:mysql://${config.host}:$port/${config.database}"
    }

    override fun configure(hikari: HikariConfig) {
        hikari.username = config.username
        hikari.password = config.password
    }
}

class PostgresDriver(val config: DatabaseConfig) : HikariDriver() {
    override val driverClass = "org.postgresql.Driver"

    override fun jdbcUrl(): String {
        val port = config.port ?: 5432u
        return "jdbc:postgresql://${config.host}:$port/${config.database}"
    }

    override fun configure(hikari: HikariConfig) {
        hikari.username = config.username
        hikari.password = config.password
    }
}

private fun getSqliteDbPath(plugin: Khs): String {
    val dir = plugin.shim.dataDirectory.toFile()
    val oldPath = File(dir, "database.db")
    val newPath = File(dir, "khs.db")

    if (oldPath.exists()) return oldPath.path

    return newPath.path
}

fun getDriver(plugin: Khs): Driver =
    when (plugin.config.database.type) {
        DatabaseType.SQLITE -> SqliteDriver(getSqliteDbPath(plugin))
        DatabaseType.MEMORY -> SqliteDriver("file::memory:?cache=shared")
        DatabaseType.MYSQL -> MysqlDriver(plugin.config.database)
        DatabaseType.POSTGRES -> PostgresDriver(plugin.config.database)
        DatabaseType.DISABLED -> error("unreachable")
    }
