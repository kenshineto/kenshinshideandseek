plugins {
	id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
	maven("https://hub.spigotmc.org/nexus/content/repositories/public/")
	maven("https://repo.codemc.io/repository/maven-releases/")
	maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:26.1-R0.1-SNAPSHOT")
	compileOnly("com.github.retrooper:packetevents-spigot:2.11.2")
	compileOnly("me.clip:placeholderapi:2.12.2")
	implementation("com.github.cryptomorin:XSeries:13.6.0")
	implementation(project(":core"))
}

kotlin {
    sourceSets.main {
		kotlin.srcDirs("src")
		resources.srcDirs("res")
    }
}

tasks.processResources {
	inputs.property("version", project.version)
	inputs.property("name", rootProject.name)

	filesMatching("plugin.yml") {
        expand(
			"version" to project.version,
			"name" to rootProject.name
		)
    }
}

tasks.shadowJar {
	archiveBaseName.set("KenshinsHideAndSeek")
	archiveClassifier.set("")

	relocate("com.cryptomorin.xseries", "cat.freya.depend.xseries")
	relocate("com.zaxxer.hikari", "cat.freya.depend.hikari")

	mergeServiceFiles {
        include("META-INF/services/java.sql.Driver")
	}

	exclude {
		it.path.startsWith("META-INF/") &&
		!it.path.startsWith("META-INF/services/") &&
		!it.path.endsWith(".kotlin_module")
	}
}
