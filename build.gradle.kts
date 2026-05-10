import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "com.vanillage.raytraceantixray"
version = "1.16.3.1"
description = "Paper plugin for server-side async multithreaded ray tracing to hide ores that are exposed to air using Paper Anti-Xray engine-mode 1."

val paperVersion = (project.findProperty("paperVersion") as String?) ?: "1.21.11-R0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    paperweight.paperDevBundle(paperVersion)
    compileOnly("com.github.retrooper:packetevents-spigot:2.12.1")
}

// Paper 1.20.5+ runtime is Mojang-mapped; ship a Mojang-mapped plugin JAR (no reobf to Spigot).
paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

sourceSets {
    named("main") {
        java.setSrcDirs(listOf("RayTraceAntiXray/src/main/java"))
        resources.setSrcDirs(listOf("RayTraceAntiXray/src/main/resources"))
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    processResources {
        filteringCharset = "UTF-8"
    }

    jar {
        archiveBaseName.set("RayTraceAntiXray")
    }
}
