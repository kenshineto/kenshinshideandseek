import org.gradle.api.Project

val Project.buildInfo: Map<String, String>
     get() = mapOf(
        "id" to rootProject.name,
        "name" to providers.gradleProperty("khs.name").get(),
        "version" to rootProject.version.toString(),
        "author" to providers.gradleProperty("khs.author").get(),
    )

fun Map<String, String>.toYaml(): String =
    entries.joinToString("\n") { (key, value) ->
        "${key}: \"${value}\""
    }
