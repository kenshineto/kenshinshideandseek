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
    compileOnly(libs.packetevents.api)
    implementation(libs.jackson.kotlin, excludeKotlin)
    implementation(libs.jackson.dataformat.yaml, excludeKotlin)

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

    // tests
    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.packetevents.api)
    testImplementation(libs.slf4j.api)
    testImplementation(libs.sqlite)
}

val buildInfo = registerBuildInfoTask()

kotlin { sourceSets { main { kotlin.srcDir(buildInfo) } } }
