plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.neogradle.userdev)
}

repositories {
    maven("https://maven.neoforged.net/releases")
}

dependencies {
    compileOnly(libs.neoforge)
    shadow(project(":mod"))
}
