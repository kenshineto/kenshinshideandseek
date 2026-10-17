pluginManagement {
    repositories {
        maven("https://maven.freya.cat")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases")
        maven("https://central.sonatype.com/repository/maven-snapshots")
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "KenshinsHideAndSeek"

include("core", "bukkit", "mod", "fabric", "neoforge")
