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
		jvmToolchain {
			languageVersion.set(JavaLanguageVersion.of(8))
		}
	}
}
