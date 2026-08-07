package cat.freya.khs
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
private data class GitHubRelease(
    @JsonAlias("tag_name")
    val tagName: String,
)

class UpdateChecker(val plugin: Khs) {
    var updateExists: Boolean = false
        private set

    var latestVersion: String? = null
        private set

    fun check() {
        // both must be set for update checking to work
        if (!plugin.config.checkForUpdates || !plugin.buildInfo.telemetry) {
            return
        }

        val endpoint = "https://api.github.com/repos/kenshineto/kenshinshideandseek/releases/latest"
        val release: GitHubRelease = plugin.fetchJson(endpoint) ?: return

        val currentVersion = plugin.buildInfo.version
        val latestVersion = release.tagName.removePrefix("v")
        plugin.shim.logger.info("Latest plugin version: $latestVersion")

        val currentParts = currentVersion.split(".").map(String::toUInt)
        val latestParts = latestVersion.split(".").map(String::toUInt)

        this.latestVersion = latestVersion
        this.updateExists = currentParts
            .zip(latestParts)
            .firstOrNull { (c, l) -> c != l }
            ?.let { (c, l) -> c < l }
            ?: (latestParts.size > currentParts.size)
    }
}
