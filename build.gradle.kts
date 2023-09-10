plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.emortal.minestom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://repo.emortal.dev/snapshots")
    maven("https://repo.emortal.dev/releases")

    maven("https://jitpack.io")
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation("dev.emortal.minestom:game-sdk:bf1bede")

    implementation("net.kyori:adventure-text-minimessage:4.12.0")
    implementation("dev.hollowcube:polar:1.2.0")

    implementation("dev.emortal:rayfast:a4a8041")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
    }
}

tasks {
    compileJava {
        options.compilerArgs.addAll(listOf(
            "--release", "20",
            "--enable-preview"
        ))
    }

    shadowJar {
        mergeServiceFiles()

        manifest {
            attributes (
                "Main-Class" to "dev.emortal.minestom.lazertag.Main",
                "Multi-Release" to true
            )
        }
    }

    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    build {
        dependsOn(shadowJar)
    }
}
