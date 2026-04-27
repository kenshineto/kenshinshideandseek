@file:Suppress("UNCHECKED_CAST")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.detekt.gradle.Detekt

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.shadow) apply false
}

group = "cat.freya.khs"

version = "2.1.1"

allprojects {
    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/public/")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://repo.codemc.io/repository/maven-releases/")
        maven("https://repo.extendedclip.com/releases/")
    }

    // only run when explicitly requested
    // i.e. dont lint on builds
    tasks.matching { it.name.contains("ktlint", ignoreCase = true) }.configureEach {
        onlyIf {
            project.gradle.startParameter.taskNames
                .any { it == "lint" || it == "format" }
        }
    }

    // only run detekt during lint
    tasks.matching { it.name == "detekt" }.configureEach {
        onlyIf {
            project.gradle.startParameter.taskNames
                .contains("lint")
        }
    }
}

subprojects {
    // jvm
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")

    // linting
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "dev.detekt")

    // make projects like cat.freya.khs.bukkit to be in
    // the .bukkit package
    if (project.name != "core") {
        group = "${rootProject.group}.${project.name}"
    }

    // we need to support java 8 so that we can support old
    // minecraft versions such as 1.8
    val jvmVersion =
        when (project.name) {
            "neoforge" -> 25
            "fabric" -> 25
            "mod" -> 25
            else -> 8
        }

    kotlin {
        jvmToolchain(jvmVersion)

        sourceSets.main {
            kotlin.srcDirs("src")
            resources.srcDirs("res")
        }
    }

    java { toolchain { languageVersion.set(JavaLanguageVersion.of(jvmVersion)) } }

    detekt {
        config.setFrom("$rootDir/detekt.yml")
        source.setFrom("src")
    }

    tasks.withType<Detekt>().configureEach {
        reports {
            html.required.set(false)
            checkstyle.required.set(false)
            sarif.required.set(false)
            markdown.required.set(false)
        }
    }

    tasks.processResources {
        val props =
            mapOf(
                "version" to providers.provider { rootProject.version.toString() },
                "name" to providers.provider { rootProject.name },
            )

        inputs.properties(props.mapValues { it.value.get() })

        val templates = listOf("**.yml", "**/*.json", "**/*.toml")
        templates.forEach { resource ->
            filesMatching(resource) { expand(props.mapValues { it.value.get() }) }
        }
    }

    tasks.withType<ShadowJar>().configureEach {
        val jarName = rootProject.name
        val jarVersion = rootProject.version.toString()
        val jarPlatform = project.name

        // calculate jar name
        archiveBaseName.set(jarName)
        archiveVersion.set(jarVersion)
        archiveClassifier.set(jarPlatform)
        destinationDirectory.set(layout.buildDirectory.dir("libs"))
        archiveFileName.set("$jarName-$jarVersion+$jarPlatform.jar")

        // we need to process resources before
        // putting them in the jar
        dependsOn(tasks.processResources)

        // only include shadow'd depends (not implementation)
        configurations = listOf(project.configurations.named("shadow").get())

        // load in image assets
        from(tasks.jar)
        from("../img") { into("assets") }

        // relocate shaded deps
        val relocations =
            listOf(
                // core
                "org.jetbrains.exposed",
                "com.zaxxer.hikari",
                // bukkit
                "com.cryptomorin.xseries",
            )
        relocations.forEach { dep ->
            val module = dep.split('.').last()
            val hasDep =
                project.configurations
                    .flatMap { it.dependencies }
                    .any { it.group == group }
            if (hasDep) {
                relocate(dep, "cat.freya.depend.$module")
            }
        }

        // multiple database drivers may collide here
        mergeServiceFiles { include("META-INF/services/java.sql.Driver") }

        // remove META-INF crap
        exclude {
            it.path.startsWith("META-INF/") &&
                !it.path.startsWith("META-INF/services/") &&
                !it.path.endsWith(".kotlin_module") &&
                !it.path.endsWith("neoforge.mods.toml")
        }
    }

    tasks.withType<ShadowJar>().all {
        tasks.findByName("assemble")?.dependsOn(this)
    }
}

tasks.named<Jar>("jar") { enabled = false }

tasks.register("lint") {
    dependsOn(subprojects.map { it.tasks.named("ktlintCheck") })
    dependsOn(tasks.named("ktlintCheck"))
    dependsOn(subprojects.map { it.tasks.named("detekt") })
}

tasks.register("format") {
    dependsOn(subprojects.map { it.tasks.named("ktlintFormat") })
    dependsOn(tasks.named("ktlintFormat"))
}
