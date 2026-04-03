@file:Suppress("UNCHECKED_CAST")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow) apply false
}

group = "cat.freya.khs"

version = "2.0.2"

allprojects { repositories { mavenCentral() } }

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")

    // make projects like cat.freya.khs.bukkit to be in
    // the .bukkit package
    if (project.name != "core") {
        group = "${rootProject.group}.${project.name}"
    }

    // we need to support java 8 so that we can support old
    // minecraft versions such as 1.8

    kotlin {
        jvmToolchain(8)

        sourceSets.main {
            kotlin.srcDirs("src")
            resources.srcDirs("res")
        }
    }

    java { toolchain { languageVersion.set(JavaLanguageVersion.of(8)) } }

    tasks.processResources {
        val props =
            mapOf(
                "version" to providers.provider { rootProject.version.toString() },
                "name" to providers.provider { rootProject.name },
            )

        inputs.properties(props.mapValues { it.value.get() })

        val projectTemplates = project.ext["templates"] as List<String>
        projectTemplates.forEach { resource ->
            filesMatching(resource) { expand(props.mapValues { it.value.get() }) }
        }
    }

    tasks.withType<ShadowJar>().configureEach {
        val jarName = rootProject.name
        val jarVersion = rootProject.version.toString()
        val jarPlatform = project.name

        dependsOn(tasks.processResources)

        archiveBaseName.set(jarName)
        archiveVersion.set(jarVersion)
        archiveClassifier.set(jarPlatform)
        destinationDirectory.set(layout.buildDirectory.dir("libs"))
        archiveFileName.set("$jarName-$jarVersion+$jarPlatform.jar")

        // only include shadow'd depends (not implementation)
        configurations = listOf(project.configurations.named("shadow").get())

        val core = project(":core")
        from(core.sourceSets.main.get().output)
        from(project.sourceSets.main.get().output)
        from("../img") { into("assets") }

        val coreRelocations = core.ext["relocations"] as List<String>
        val projectRelocations = project.ext["relocations"] as List<String>
        (coreRelocations + projectRelocations).forEach { dep ->
            val module = dep.split('.').last()
            relocate(dep, "cat.freya.depend.$module")
        }

        mergeServiceFiles { include("META-INF/services/java.sql.Driver") }

        exclude {
            it.path.startsWith("META-INF/") &&
                !it.path.startsWith("META-INF/services/") &&
                !it.path.endsWith(".kotlin_module")
        }
    }

    tasks.named("build") { dependsOn(tasks.withType<ShadowJar>()) }
}

tasks.named<Jar>("jar") { enabled = false }

tasks.build {
    dependsOn(
        subprojects
            .filter { it.tasks.findByName("shadowJar") != null }
            .map { it.tasks.named("build") }
    )
}
