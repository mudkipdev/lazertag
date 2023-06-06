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
//    implementation("dev.hollowcube:polar:1.1.1")

    implementation("dev.emortal", "rayfast", "d198fa1")
    implementation("dev.emortal.tnt", "TNT", "75546f5")
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
