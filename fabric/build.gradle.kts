plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.architectury.loom)
    alias(libs.plugins.architectury.plugin)
}

architectury {
    platformSetupLoomIde()
    fabric()
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
    named("developmentFabric") {
        extendsFrom(common)
    }
}

dependencies {
    minecraft(libs.minecraft)
    compileOnly(libs.fabric.loader)

    common(project(":mod")) { isTransitive = false }
    shadowBundle(project(":mod", configuration = "transformProductionFabric"))
}
