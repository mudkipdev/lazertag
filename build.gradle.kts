plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.emortal.minestom"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("dev.emortal.minestom", "core", "85ae46e")
    implementation("dev.emortal.minestom", "game-sdk", "4d22719")

    implementation("net.kyori:adventure-text-minimessage:4.12.0")
    implementation("dev.hollowcube:polar:1.2.0")

    implementation("dev.emortal", "rayfast", "d198fa1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
