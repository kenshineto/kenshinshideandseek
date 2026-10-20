plugins {
    alias(libs.plugins.fabric.loom)
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
}
