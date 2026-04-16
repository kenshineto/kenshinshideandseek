plugins { alias(libs.plugins.shadow) }

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    // bukkit does not provide kotlin
    shadow(libs.kotlin.stdlib)
    shadow(libs.kotlin.reflect)
    shadow(libs.kotlinx.coroutines.core)

    compileOnly(libs.spigot.api)
    compileOnly(libs.packetevents.spigot)
    compileOnly(libs.placeholderapi)
    shadow(libs.xseries)
    shadow(project(":core"))
}
