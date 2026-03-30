plugins {
	id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
	maven("https://hub.spigotmc.org/nexus/content/repositories/public/")
	maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:26.1-R0.1-SNAPSHOT")
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

	minimize {
		exclude(dependency("org.jetbrains.kotlin:kotlin-reflect"))
		exclude(dependency("org.jetbrains.exposed:.*"))
		exclude(dependency("com.mysql:mysql-connector-j"))
		exclude(dependency("org.postgresql:postgresql"))
	}

	mergeServiceFiles {
        include("META-INF/services/java.sql.Driver")
	}

	exclude("org/slf4j/**")
	exclude {
		it.path.startsWith("META-INF/") &&
		!it.path.startsWith("META-INF/services/") &&
		!it.path.endsWith(".kotlin_module")
	}
}
