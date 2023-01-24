plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.emortal.minestom"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("dev.emortal.minestom", "core", "b875988")
    implementation("dev.emortal.minestom", "game-sdk", "fba7a26")

    implementation("com.github.Minestom", "Minestom", "c995f9c3a9")
}

tasks {
    shadowJar {
        mergeServiceFiles()
    }
    build {
        dependsOn(shadowJar)
    }
}
