plugins {
    alias(libs.plugins.architectury.loom)
    alias(libs.plugins.architectury.plugin)
}

architectury {
    platformSetupLoomIde()
    fabric()
}

lodestone {
    resources("fabric.mod.json")
    shadow()
}

dependencies {
    minecraft(libs.minecraft)
    compileOnly(libs.fabric.api)
    compileOnly(libs.fabric.loader)

    shadow(project(":mod", configuration = "transformProductionFabric"))
}
