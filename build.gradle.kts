import cat.freya.lodestone.getVersion
import cat.freya.lodestone.publishing.License
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.time.Instant

plugins {
    alias(libs.plugins.lodestone)
}

group = "cat.freya.khs"

version = "2.4.0-SNAPSHOT"

val gitCommitHash: Provider<String> =
    providers
        .exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
        }
        .standardOutput
        .asText
        .map { it.trim() }

allprojects {
    apply(plugin = "cat.freya.lodestone")

    lodestone {
        var jvmVersion =
            when (project.name) {
                "neoforge",
                "fabric",
                "mod" -> 25
                else -> 8
            }
        java(jvmVersion)
        kotlin()
        buildInfo {
            put("id", rootProject.name)
            put("version", rootProject.version)
            put("name", "Kenshin's Hide and Seek")
            put("author", "KenshinEto")
            put("fabricapi", getVersion("fabric-api"))
            put("fabricloader", getVersion("fabric-loader"))
            put("minecraft", getVersion("minecraft"))
            put("packetevents", getVersion("packetevents"))
            put("java", jvmVersion)
            put("telemetry", false)
            put("bstatsId", 0)
            put("gitRev", gitCommitHash.get())
            put("buildDate", Instant.now().toString())
        }
        // configure shadow but
        // dont apply just yet
        shadow(false) {
            includeProjectName = true
            // core
            relocations.add("com.fasterxml.jackson")
            relocations.add("com.zaxxer.hikari")
            relocations.add("org.jetbrains.exposed")
            relocations.add("org.yaml.snakeyaml")
            // bukkit
            relocations.add("com.cryptomorin.xseries")
            relocations.add("org.bstats")
        }
        detekt()
        spotless()
        kover()
    }

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/public/")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://repo.codemc.io/repository/maven-releases/")
        maven("https://repo.extendedclip.com/releases/")
        maven("https://maven.freya.cat/snapshots")
    }
}

subprojects {
    version = rootProject.version

    // make projects like cat.freya.khs.bukkit to be in
    // the .bukkit package
    if (project.name != "core") {
        group = "${rootProject.group}.${project.name}"
    }

    tasks.withType<ShadowJar>().configureEach {
        // load in image assets
        from("../img") { into("assets") }
    }

    lodestone {
        publish {
            artifact = project.name
            group = "cat.freya.khs"
            author = "KenshinEto"
            license = License.AGPL3
            gitRepo = "https://g.freya.cat/kenshinshideandseek"
            reposilite("https://maven.freya.cat")
        }
    }
}

tasks.register("publish") {
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("publish") })
}
