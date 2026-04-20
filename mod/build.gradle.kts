plugins {
    // this is only used to get
    // net.minecraft bindings
    alias(libs.plugins.fabric.loom)
}

repositories {
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://maven.architectury.dev/")
}

dependencies {
    minecraft(libs.minecraft)
    implementation(project(":core"))
    compileOnly(libs.slf4j.api)
    compileOnly(libs.luckperms.api)
    compileOnly(libs.packetevents.api)

    // compileOnly(libs.architectury)
    compileOnly(files("libs/architectury.jar"))

    // fabric or forge do not shade sqlite
    // or snakeyml
    implementation(libs.sqlite)
    implementation(libs.snakeyaml)
}
