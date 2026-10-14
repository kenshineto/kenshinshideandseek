lodestone {
    resources("plugin.yml")
    shadow {
        includeKotlin = true
    }
}

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.packetevents.spigot)
    compileOnly(libs.placeholderapi)
    shadow(libs.xseries)
    shadow(libs.bstats.bukkit)
    shadow(project(":core"))
}

configurations.configureEach {
    exclude(group = "org.xerial", module = "sqlite-jdbc")
}
