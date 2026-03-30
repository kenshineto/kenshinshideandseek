dependencies {
	// core libs
	compileOnly("org.yaml:snakeyaml:2.6")
	implementation(kotlin("reflect"))

	// orm
	implementation("org.jetbrains.exposed:exposed-core:1.1.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.1.1")

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
