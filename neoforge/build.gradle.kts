plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.architectury.loom)
    alias(libs.plugins.architectury.plugin)
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

val common by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

val shadowBundle by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

configurations {
    named("compileClasspath") {
        extendsFrom(common)
    }
    named("runtimeClasspath") {
        extendsFrom(common)
    }
    named("developmentNeoForge") {
        extendsFrom(common)
    }
}

dependencies {
    minecraft(libs.minecraft)
    neoForge(libs.neoforge)

    common(project(":mod")) { isTransitive = false }
    shadowBundle(project(":mod", configuration = "transformProductionNeoForge"))
}
