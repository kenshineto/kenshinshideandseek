plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.fabric.loader)
    shadow(project(":mod"))
}
