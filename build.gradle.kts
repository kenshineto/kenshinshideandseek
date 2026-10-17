import cat.freya.lodestone.getKotlinVersion
import cat.freya.lodestone.getVersion
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.lodestone)
}

group = "cat.freya.khs"

version = "2.3.0"

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
        jvm(jvmVersion)
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
            put("kotlin", getKotlinVersion())
            put("telemetry", false)
            put("bstatsId", 0)
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
        maven("https://maven.freya.cat")
    }
}

subprojects {
    // make projects like cat.freya.khs.bukkit to be in
    // the .bukkit package
    if (project.name != "core") {
        group = "${rootProject.group}.${project.name}"
    }

    tasks.withType<ShadowJar>().configureEach {
        // load in image assets
        from("../img") { into("assets") }
    }
}
