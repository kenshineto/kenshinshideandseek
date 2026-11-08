package cat.freya.khs

import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.event.Level
import org.slf4j.helpers.LegacyAbstractLogger
import org.slf4j.helpers.MessageFormatter

abstract class KhsLogger(val plugin: Khs) : LegacyAbstractLogger() {
    override fun getFullyQualifiedCallerName(): String = BuildInfo.id

    override fun isErrorEnabled(): Boolean {
        return true
    }

    override fun isWarnEnabled(): Boolean {
        return true
    }

    override fun isInfoEnabled(): Boolean {
        return true
    }

    override fun isDebugEnabled(): Boolean {
        return plugin.config.debug
    }

    override fun isTraceEnabled(): Boolean {
        return plugin.config.debug
    }

    override fun handleNormalizedLoggingCall(
        level: Level?,
        marker: Marker?,
        messagePattern: String?,
        arguments: Array<out Any?>?,
        throwable: Throwable?,
    ) {
        val message = MessageFormatter.basicArrayFormat(messagePattern, arguments ?: emptyArray()) ?: return
        when (level) {
            Level.ERROR if isErrorEnabled -> `khs$error`(message)
            Level.WARN if isWarnEnabled -> `khs$warn`(message)
            Level.INFO if isInfoEnabled -> `khs$info`(message)
            Level.DEBUG if isDebugEnabled -> `khs$info`("[DEBUG] $message")
            Level.TRACE if isTraceEnabled -> `khs$info`("[TRACE] $message")
            else -> {}
        }
    }

    protected abstract fun `khs$error`(message: String)

    protected abstract fun `khs$warn`(message: String)

    protected abstract fun `khs$info`(message: String)
}

private class KhsSlf4jLogger(plugin: Khs, private val inner: org.slf4j.Logger) : KhsLogger(plugin) {
    override fun `khs$error`(message: String) {
        inner.error(message)
    }

    override fun `khs$warn`(message: String) {
        inner.warn(message)
    }

    override fun `khs$info`(message: String) {
        inner.info(message)
    }
}

private class KhsJulLogger(plugin: Khs, private val inner: java.util.logging.Logger) : KhsLogger(plugin) {
    override fun `khs$error`(message: String) {
        inner.log(java.util.logging.Level.SEVERE, message)
    }

    override fun `khs$warn`(message: String) {
        inner.log(java.util.logging.Level.WARNING, message)
    }

    override fun `khs$info`(message: String) {
        inner.log(java.util.logging.Level.INFO, message)
    }
}

private class KhsConsoleLogger(plugin: Khs) : KhsLogger(plugin) {
    override fun `khs$error`(message: String) {
        System.err.println("[ERROR] $message")
    }

    override fun `khs$warn`(message: String) {
        System.err.println("[WARN] $message")
    }

    override fun `khs$info`(message: String) {
        println(message)
    }
}

fun getLogger(plugin: Khs, impl: Any?): KhsLogger {
    return when (impl) {
        is org.slf4j.Logger -> KhsSlf4jLogger(plugin, impl)
        is java.util.logging.Logger -> KhsJulLogger(plugin, impl)
        null -> KhsConsoleLogger(plugin)
        else -> error("Unsupported logger type: ${impl.javaClass.name}")
    }
}
