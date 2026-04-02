repositories {
	maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
	// core libs
	implementation(kotlin("reflect"))
	compileOnly("org.yaml:snakeyaml:2.6")
	compileOnly("com.github.retrooper:packetevents-api:2.11.2")
	compileOnly("com.google.guava:guava:33.5.0-jre")

	// orm
	implementation("org.jetbrains.exposed:exposed-core:1.2.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.2.0")

	// database
	compileOnly("org.xerial:sqlite-jdbc:3.51.3.0")
	implementation("com.mysql:mysql-connector-j:9.6.0")
	implementation("org.postgresql:postgresql:42.7.10")
	implementation("com.zaxxer:HikariCP:4.0.3")
}

kotlin {
    sourceSets.main {
		kotlin.srcDirs("src")
		resources.srcDirs("res")
    }
}
