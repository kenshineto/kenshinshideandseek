plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.fabric.loom)
}

repositories { maven("https://maven.fabricmc.net/") }

dependencies {
    minecraft(libs.minecraft)
    compileOnly(libs.fabric.loader)
    compileOnly(libs.fabric.api)
    compileOnly(libs.fabric.language.kotlin)
    compileOnly(libs.slf4j.api)
    shadow(project(":core"))
}

ext["relocations"] = listOf<String>()

ext["templates"] = listOf("fabric.mod.json")
