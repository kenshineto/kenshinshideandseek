dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

pluginManagement {
	repositories {
		maven("https://maven.fabricmc.net/")
	    mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "KenshinsHideAndSeek"
include("core", "bukkit", "fabric")
