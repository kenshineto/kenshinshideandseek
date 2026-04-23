plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.neogradle.userdev)
}

dependencies {
    compileOnly(libs.neoforge)
    shadow(project(":mod"))
}
