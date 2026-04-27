plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.architectury.loom)
    alias(libs.plugins.architectury.plugin)
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

dependencies {
    minecraft(libs.minecraft)
    neoForge(libs.neoforge)

    shadow(project(":mod"))
    shadow(project(":mod", configuration = "transformProductionNeoForge"))
}
