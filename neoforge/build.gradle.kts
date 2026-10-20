plugins {
    alias(libs.plugins.neoforge.moddev)
}

lodestone {
    resources("META-INF/neoforge.mods.toml")
    shadow()
}

neoForge {
    version = libs.neoforge.get().version
}

dependencies {
    shadow(project(":core"))
    shadow(project(":mod"))
}
