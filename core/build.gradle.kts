repositories { maven("https://repo.codemc.io/repository/maven-releases/") }

val excludeKotlin: ExternalModuleDependency.() -> Unit = {
    exclude(group = "org.jetbrains.kotlin")
    exclude(group = "org.jetbrains.kotlinx")
    exclude(group = "org.slf4j")
}

dependencies {
    // kotlin
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.reflect)

    // libs
    compileOnly(libs.snakeyaml)
    compileOnly(libs.packetevents.api)

    // orm
    implementation(libs.exposed.core, excludeKotlin)
    implementation(libs.exposed.jdbc, excludeKotlin)

    // database
    compileOnly(libs.sqlite)
    implementation(libs.mysql)
    implementation(libs.postgres)
    implementation(libs.hikari) {
        exclude(group = "org.slf4j")
    }
}
