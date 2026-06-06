package cat.freya.khs
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.URI

@JsonIgnoreProperties(ignoreUnknown = true)
private data class GitHubRelease(
    @JsonAlias("tag_name")
    val tagName: String,
)

class UpdateChecker(val plugin: Khs) {
    private val repo = "kenshineto/kenshinshideandseek2"
    private val endpoint = "https://api.github.com/repos/$repo/releases/latest"

    var latestVersion: String? = null
        private set

    var updateExists: Boolean = false
        private set

    private fun getLatestGitHubRelease(): GitHubRelease? {
        if (!plugin.config.checkForUpdates) {
            return null
        }

        return runCatching {
            val connection = URI(endpoint).toURL().openConnection()
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", plugin.buildInfo.id)

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val mapper = jacksonObjectMapper()
            return mapper.readValue(response, GitHubRelease::class.java)
        }.onFailure {
            plugin.shim.logger.warning("Failed to fetch latest github release: ${it.message}")
        }.getOrDefault(null)
    }

    private fun parseVersionString(version: String): List<UInt> {
        return version.split(".").map { it.toUInt() }
    }

    private fun doesUpdateExist(latestString: String, currentString: String): Boolean {
        val latest = parseVersionString(latestString)
        val current = parseVersionString(currentString)
        val count = minOf(current.size, latest.size)

        for (i in 0 until count) {
            val c = current[i]
            val l = latest[i]

            if (c < l) return true
            if (c > l) return false
        }

        return latest.size > current.size
    }

    fun check() {
        val release = getLatestGitHubRelease() ?: return

        val currentVersion = plugin.buildInfo.version
        val latestVersion = release.tagName.removePrefix("v")
        plugin.shim.logger.info("Latest plugin version: $latestVersion")

        this.latestVersion = latestVersion
        this.updateExists = doesUpdateExist(latestVersion, currentVersion)
    }
}
