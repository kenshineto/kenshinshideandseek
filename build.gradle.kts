plugins {
	kotlin("jvm") version "2.2.21"
}

allprojects {
	group = "cat.freya.khs"
	version = "2.0.0"

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

	// we need to support java 8 so that we can support old
	// minecraft versions such as 1.8

	kotlin {
		jvmToolchain(8)
	}

	java {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(8))
		}
	}
}
