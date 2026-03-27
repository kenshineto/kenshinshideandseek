plugins {
	id("com.gradleup.shadow") version "8.3.1"
}

repositories {
	maven("https://hub.spigotmc.org/nexus/content/repositories/public/")
	maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.11-R0.1-SNAPSHOT")
	compileOnly("com.github.retrooper:packetevents-spigot:2.11.2")
	implementation("com.github.cryptomorin:XSeries:13.6.0")
	implementation(project(":core"))
}

kotlin {
    sourceSets.main {
		kotlin.srcDirs("src")
		resources.srcDirs("res")
    }
}

tasks.shadowJar {
	archiveBaseName.set("KenshinsHideAndSeek")
	archiveClassifier.set("")
}
