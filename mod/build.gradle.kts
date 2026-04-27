plugins {
    alias(libs.plugins.architectury.loom)
    alias(libs.plugins.architectury.plugin)
}

architectury {
    common(listOf("fabric", "neoforge"))
}

dependencies {
    minecraft(libs.minecraft)
    implementation(project(":core"))

    // we depend on this to get @Mixin
    // do NOT use any other code from Fabric Loader
    compileOnly(libs.fabric.loader)

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
