plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.emortal.minestom"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("dev.emortal.minestom", "core", "2f8f6ea")
    implementation("dev.emortal.minestom", "game-sdk", "d11726e")

    implementation("net.kyori:adventure-text-minimessage:4.12.0")
    implementation("com.github.EmortalMC:TNT:4ef1b53482")
}

tasks {
    shadowJar {
        mergeServiceFiles()
    }
    build {
        dependsOn(shadowJar)
    }
}
