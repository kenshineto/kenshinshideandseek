plugins {
	kotlin("jvm") version "2.2.0"
}

allprojects {
	group = "cat.freya.khs"
	version = "2.0.0-alpha2"

	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")

	dependencies {
		implementation(kotlin("stdlib"))
	}

	kotlin {
		jvmToolchain(8)
	}

	java {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(8))
		}
	}
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

	// core libs
	implementation("org.yaml:snakeyaml:2.6")
	implementation(kotlin("reflect"))

	// orm
	implementation("org.jetbrains.exposed:exposed-core:1.1.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.1.1")

	// database
	implementation("org.xerial:sqlite-jdbc:3.51.3.0")
	implementation("com.mysql:mysql-connector-j:9.6.0")
	implementation("org.postgresql:postgresql:42.7.10")
	implementation("com.zaxxer:HikariCP:4.0.3")
}
