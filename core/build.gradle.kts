import cat.freya.lodestone.excludeKotlin

group = "cat.freya.khs"

lodestone {
    generateBuildInfo()
    junit()
}

dependencies {
    // libs
    compileOnly(libs.packetevents.api)
    compileOnly(libs.slf4j.api)
    implementation(libs.alloy)
    implementation(libs.exposed.core, excludeKotlin)
    implementation(libs.exposed.jdbc, excludeKotlin)

    // tests
    testImplementation(libs.packetevents.api)
    testImplementation(libs.slf4j.api)
}
