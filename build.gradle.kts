@file:Suppress("UNCHECKED_CAST")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.detekt.gradle.Detekt
import org.gradle.api.attributes.java.TargetJvmVersion

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.kotlinx.kover)
}

group = "cat.freya.khs"

version = "2.2.0"

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

    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "dev.detekt")
    apply(plugin = "org.jetbrains.kotlinx.kover")

    detekt {
        config.setFrom("$rootDir/detekt.yml")
        source.setFrom("src", "test")
    }

    tasks.withType<Detekt>().configureEach {
        reports {
            html.required.set(false)
            checkstyle.required.set(false)
            sarif.required.set(false)
            markdown.required.set(false)
        }
    }

    spotless {
        kotlin {
            ktfmt().kotlinlangStyle().configure {
                it.setMaxWidth(120)
                it.setBlockIndent(4)
                it.setContinuationIndent(4)
                it.setRemoveUnusedImports(true)
            }
        }
        kotlinGradle {
            ktfmt().kotlinlangStyle().configure {
                it.setMaxWidth(120)
                it.setBlockIndent(4)
                it.setContinuationIndent(4)
                it.setRemoveUnusedImports(true)
            }
        }
        yaml {
            target("**/*.yml")
            jackson()
                .yamlFeature("WRITE_DOC_START_MARKER", false)
                .yamlFeature("INDENT_ARRAYS_WITH_INDICATOR", true)
                .yamlFeature("LITERAL_BLOCK_STYLE", true)
        }
        json {
            target("**/*.json")
            simple().indentWithSpaces(4)
        }
        freshmark {
            target("**/*.md")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    // dont check on builds
    tasks.named("build") {
        dependsOn.removeIf { it.toString().contains("check") }
    }
}

subprojects {
    // jvm
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")

    // make projects like cat.freya.khs.bukkit to be in
    // the .bukkit package
    if (project.name != "core") {
        group = "${rootProject.group}.${project.name}"
    }

    // we need to support java 8 so that we can support old bukkit
    val jvmVersion =
        when (project.name) {
            "neoforge",
            "fabric",
            "mod" -> getModernJvmVersion()
            else -> 8
        }

    kotlin {
        jvmToolchain(jvmVersion)

        sourceSets {
            main {
                kotlin.srcDirs("src")
                resources.srcDirs("res")
            }
            test {
                kotlin.srcDirs("test")
            }
        }
    }

    java { toolchain { languageVersion.set(JavaLanguageVersion.of(jvmVersion)) } }

    val mockitoAgent = configurations.create("mockitoAgent")
    tasks.test {
        useJUnitPlatform()
        jvmArgs.add("-javaagent:${mockitoAgent.asPath}")
        javaLauncher = javaToolchains.launcherFor {
            languageVersion = JavaLanguageVersion.of(getModernJvmVersion())
        }
    }

    // use the modern jvm version for tests in the core module
    // instead of just using java 8
    configurations
        .matching { it.name in setOf("testCompileClasspath", "testRuntimeClasspath") }
        .configureEach {
            attributes { attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, getModernJvmVersion()) }
        }

    dependencies {
        testImplementation(rootProject.libs.junit.jupiter.api)
        testRuntimeOnly(rootProject.libs.junit.jupiter.engine)
        testRuntimeOnly(rootProject.libs.junit.platform.launcher)
        testImplementation(rootProject.libs.mockito.core)
        mockitoAgent(rootProject.libs.mockito.core) { isTransitive = false }
    }

    tasks.processResources {
        inputs.properties(rootProject.getBuildInfo())

        val templates = listOf("**.yml", "**/*.json", "**/*.toml")
        templates.forEach { resource ->
            filesMatching(resource) { expand(rootProject.getBuildInfo()) }
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
            setOf(
                // core
                "org.bstats",
                "com.fasterxml.jackson",
                "org.jetbrains.exposed",
                "org.yaml.snakeyaml",
                "com.zaxxer.hikari",
                // bukkit
                "com.cryptomorin.xseries",
            )

        relocations.forEach { pkg ->
            runCatching {
                // try to relocate and ignore on failure
                val module = pkg.split('.').last()
                relocate(pkg, "cat.freya.depend.$module")
            }
        }

        // multiple database drivers may collide here
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        mergeServiceFiles { include("META-INF/services/java.sql.Driver") }

        // remove META-INF crap
        exclude {
            it.path.startsWith("META-INF/") &&
                !it.path.startsWith("META-INF/services/") &&
                !it.path.endsWith(".kotlin_module") &&
                !it.path.endsWith("neoforge.mods.toml")
        }
    }

    tasks.withType<ShadowJar>().all { tasks.findByName("assemble")?.dependsOn(this) }
}

dependencies {
    subprojects.forEach {
        kover(project(it.path))
    }
}

tasks.named<Jar>("jar") { enabled = false }

tasks.register("lint") {
    dependsOn(subprojects.map { it.tasks.named("spotlessCheck") })
    dependsOn(tasks.named("spotlessCheck"))
    dependsOn(subprojects.map { it.tasks.named("detekt") })
}

tasks.register("format") {
    dependsOn(subprojects.map { it.tasks.named("spotlessApply") })
    dependsOn(tasks.named("spotlessApply"))
}

tasks.register("coverage") {
    dependsOn(tasks.named("koverHtmlReport"))
}
