import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "com.vanillage.raytraceantixray"
version = "1.16.3.4"
description = "Paper plugin for server-side async multithreaded ray tracing to hide ores that are exposed to air using Paper Anti-Xray engine-mode 1."

val paperVersion = (project.findProperty("paperVersion") as String?) ?: "1.21.11-R0.1-SNAPSHOT"

java {
    toolchain {
        // Paper 26.1+ userdev runs Paperclip with the toolchain JDK; requires Java 25+.
        languageVersion.set(JavaLanguageVersion.of(25))
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

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testImplementation(paperweight.paperDevBundle(paperVersion))
    testImplementation(sourceSets.main.get().output.classesDirs)
}

// Paper 1.20.5+ runtime is Mojang-mapped; ship a Mojang-mapped plugin JAR (no reobf to Spigot).
paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

sourceSets {
    named("main") {
        java.setSrcDirs(listOf("RayTraceAntiXray/src/main/java"))
        resources.setSrcDirs(listOf("RayTraceAntiXray/src/main/resources"))
    }
    named("test") {
        java.setSrcDirs(listOf("RayTraceAntiXray/src/test/java"))
        resources.setSrcDirs(listOf("RayTraceAntiXray/src/test/resources"))
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(25)
    }

    compileTestJava {
        options.encoding = "UTF-8"
        options.release.set(25)
    }

    test {
        useJUnitPlatform {
            excludeTags("bench")
        }
    }

    register<Test>("bench") {
        group = "verification"
        description = "Section-leap vs pure DDA micro-benchmark (prints timing table to stdout)"
        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath
        useJUnitPlatform {
            includeTags("bench")
        }
        testLogging {
            showStandardStreams = true
            events("passed")
        }
    }

    processResources {
        filteringCharset = "UTF-8"
    }

    jar {
        archiveBaseName.set("RayTraceAntiXray")
    }
}
