import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

fun Project.getVersion(name: String): String {
        val libs = extensions
                .getByType(VersionCatalogsExtension::class.java)
                .named("libs")

        return libs.findVersion(name).get().requiredVersion.replace(".+", "")
}

fun Project.getBuildInfo(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        // plugin meta
        map["id"] = rootProject.name
        map["version"] = rootProject.version.toString()
        map["name"] = providers.gradleProperty("khs.name").get()
        map["author"] = providers.gradleProperty("khs.author").get()

        // versions
        map["minecraft"] = getVersion("minecraft")
        map["fabricloader"] = getVersion("fabric-loader")
        map["packetevents"] = getVersion("packetevents")
        map["architectury"] = getVersion("architectury")

        // telemetry
        map["telemetry"] = providers.gradleProperty("khs.telemetry").map(String::toBoolean).getOrElse(false)
        map["bstatsId"] = providers.gradleProperty("khs.bstatsId").map(String::toIntOrNull).getOrElse(0)

        return map
}

fun Project.getBuildInfoYaml(): String = buildString {
        val buildInfo = getBuildInfo()
        buildInfo.entries.forEach { (key, value) ->
                if (value is String) {
                        appendLine("${key}: \"${value}\"")
                } else {
                        appendLine("${key}: ${value}")
                }
        }
}
