plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.emortal.minestom"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("dev.emortal.minestom", "core", "06317cc")
    implementation("dev.emortal.minestom", "game-sdk", "6502476")

    implementation("net.kyori:adventure-text-minimessage:4.12.0")
    implementation("com.github.EmortalMC:TNT:4ef1b53482")

    implementation("dev.emortal", "rayfast", "d198fa1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    shadowJar {
        mergeServiceFiles()
    }
    build {
        dependsOn(shadowJar)
    }
}
