import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "com.vanillage.raytraceantixray"
version = "1.17.0"
description = "Paper plugin for server-side async multithreaded ray tracing to hide ores that are exposed to air using Paper Anti-Xray engine-mode 1."

data class PaperTarget(
    val paperVersion: String,
    val minecraftVersion: String,
    val javaVersion: Int,
    val apiVersion: String,
    val nmsSourceDir: String,
)

val paperTargets = mapOf(
    "1.21.11" to PaperTarget(
        paperVersion = "1.21.11-R0.1-SNAPSHOT",
        minecraftVersion = "1.21.11",
        javaVersion = 21,
        apiVersion = "1.21.11",
        nmsSourceDir = "RayTraceAntiXray/src/nms/paper-1.21.11/java",
    ),
    "26.1.2" to PaperTarget(
        paperVersion = "26.1.2.build.65-stable",
        minecraftVersion = "26.1.2",
        javaVersion = 25,
        apiVersion = "26.1.2",
        nmsSourceDir = "RayTraceAntiXray/src/nms/paper-26.1.2/java",
    ),
)

val paperTargetName = (findProperty("paperTarget") as String?) ?: "26.1.2"
val paperTarget = paperTargets[paperTargetName]
    ?: throw GradleException("Unknown paperTarget '$paperTargetName'. Supported: ${paperTargets.keys.sorted()}")

extra["paperTarget"] = paperTargetName
extra["minecraftVersion"] = paperTarget.minecraftVersion

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(paperTarget.javaVersion))
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    paperweight.paperDevBundle(paperTarget.paperVersion)
    compileOnly("com.github.retrooper:packetevents-spigot:2.12.1")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testImplementation(paperweight.paperDevBundle(paperTarget.paperVersion))
    testImplementation(sourceSets.main.get().output.classesDirs)
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

sourceSets {
    named("main") {
        java.setSrcDirs(
            listOf(
                "RayTraceAntiXray/src/main/java",
                paperTarget.nmsSourceDir,
            ),
        )
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
        options.release.set(paperTarget.javaVersion)
    }

    compileTestJava {
        options.encoding = "UTF-8"
        options.release.set(paperTarget.javaVersion)
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
        filesMatching("plugin.yml") {
            expand(
                mapOf(
                    "apiVersion" to paperTarget.apiVersion,
                    "pluginVersion" to project.version,
                ),
            )
        }
    }

    jar {
        archiveBaseName.set("RayTraceAntiXray")
        archiveClassifier.set(paperTargetName)
    }
}
