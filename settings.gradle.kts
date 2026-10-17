pluginManagement {
    repositories {
        maven("https://maven.freya.cat/snapshots")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases")
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "KenshinsHideAndSeek"

include("core", "bukkit", "mod", "fabric", "neoforge")
