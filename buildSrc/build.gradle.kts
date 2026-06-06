plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

sourceSets {
    main {
        kotlin.srcDirs("src")
    }
}
