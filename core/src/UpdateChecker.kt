package cat.freya.khs

import cat.freya.alloy.fetchJson
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true) private data class GitHubRelease(@JsonAlias("tag_name") val tagName: String)

class UpdateChecker(val plugin: Khs) {
    var updateExists: Boolean = false
        private set

    var latestVersion: Version? = null
        private set

    fun check() {
        if (!plugin.config.checkForUpdates || Version(BuildInfo.version).isSnapshot) {
            return
        }

        val endpoint = "https://api.github.com/repos/kenshineto/kenshinshideandseek/releases/latest"
        val userAgent = "${BuildInfo.id} ${BuildInfo.version}"
        val release: GitHubRelease = fetchJson<GitHubRelease>(endpoint, userAgent).getOrDefault(null) ?: return

        val currentVersion = Version(BuildInfo.version)
        val latestVersion = Version(release.tagName.removePrefix("v"))

        plugin.logger.info("Latest plugin version: $latestVersion")

        this.latestVersion = latestVersion
        this.updateExists = latestVersion > currentVersion
    }
}
