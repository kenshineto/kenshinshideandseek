plugins {
    alias(libs.plugins.fabric.loom)
}

lodestone {
    resources("fabric.mod.json")
    shadow()
}

dependencies {
    minecraft(libs.minecraft)
    compileOnly(libs.fabric.api)
    compileOnly(libs.fabric.loader)
    shadow(project(":core"))
    shadow(project(":mod"))
}
