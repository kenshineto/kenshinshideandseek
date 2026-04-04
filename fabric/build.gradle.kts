plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.fabric.loom)
}

repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    minecraft(libs.minecraft)
    compileOnly(libs.fabric.loader)
    compileOnly(libs.fabric.api)
    compileOnly(libs.fabric.language.kotlin)
    compileOnly(libs.slf4j.api)
    compileOnly(libs.luckperms.api)
    compileOnly(libs.packetevents.fabric)
    shadow(project(":core"))
}

ext["relocations"] = listOf<String>()

ext["templates"] = listOf("fabric.mod.json")
