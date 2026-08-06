import org.gradle.api.Project

val Project.buildInfo: Map<String, String>
        get() = mapOf(
                "id" to rootProject.name,
                "version" to rootProject.version.toString(),
                "name" to providers.gradleProperty("khs.name").get(),
                "author" to providers.gradleProperty("khs.author").get()
        )


fun Project.getBuildInfoYaml(): String = buildString {
        buildInfo.entries.forEach { (key, value) ->
                appendLine("${key}: \"${value}\"")
        }

        val telemetry = providers.gradleProperty("khs.telemetry").map(String::toBoolean).getOrElse(false)
        appendLine("telemetry: ${telemetry}")

        val bstatsId = providers.gradleProperty("khs.bstatsId").map(String::toIntOrNull).getOrElse(0)
        appendLine("bstatsId: ${bstatsId}")
}
