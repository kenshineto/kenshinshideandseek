plugins {
    alias(libs.plugins.shadow)
}

repositories {
    maven("https://maven.fabricmc.net/")
}

dependencies {
    compileOnly(libs.fabric.loader)
    shadow(project(":mod"))
}
