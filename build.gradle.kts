plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.emortal.minestom"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("dev.emortal.minestom", "core", "c0305b3")
    implementation("dev.emortal.minestom", "game-sdk", "fec1d81")

    implementation("net.kyori:adventure-text-minimessage:4.12.0")
//    implementation("dev.hollowcube:polar:1.1.1")

    implementation("dev.emortal", "rayfast", "bb9d190")
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
