plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.architectury.loom)
    alias(libs.plugins.architectury.plugin)
}

architectury {
    platformSetupLoomIde()
    fabric()
}

dependencies {
    minecraft(libs.minecraft)
    compileOnly(libs.fabric.loader)

    shadow(project(":mod"))
    shadow(project(":mod", configuration = "transformProductionFabric"))
}
