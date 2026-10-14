plugins {
    alias(libs.plugins.architectury.loom)
    alias(libs.plugins.architectury.plugin)
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

lodestone {
    resources("META-INF/neoforge.mods.toml")
    shadow()
}

dependencies {
    minecraft(libs.minecraft)
    neoForge(libs.neoforge)

    shadow(project(":mod", configuration = "transformProductionNeoForge"))
}
