import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    `my-conventions`
    id("io.papermc.paperweight.userdev")
    id("com.gradleup.shadow") version "9.3.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "com.vanillage.raytraceantixray"
version = "1.17.6"
description = "Paper plugin for server-side async multithreaded ray tracing to hide ores that are exposed to air using Paper Anti-Xray engine-mode 1."

java {
    disableAutoTargetJvm()
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.faststats.dev/releases")
}

dependencies {
    paperweight.paperDevBundle("26.2.build.56-alpha")
    compileOnly("com.github.retrooper:packetevents-spigot:2.12.1")
    implementation("org.bstats:bstats-bukkit:3.2.1")
    implementation("dev.faststats.metrics:bukkit:0.27.1")

    runtimeOnly(project(":paper_1_21_11"))
    runtimeOnly(project(":paper_26_1_2"))
    runtimeOnly(project(":paper_26_2"))

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testImplementation(paperweight.paperDevBundle("26.2.build.56-alpha"))
    testImplementation(sourceSets.main.get().output.classesDirs)
}

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
        options.release.set(21)
    }

    compileTestJava {
        options.encoding = "UTF-8"
        options.release.set(21)
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
                    "pluginVersion" to project.version,
                ),
            )
        }
    }

    jar {
        archiveBaseName.set("RayTraceAntiXray")
        archiveClassifier.set("plain")
        manifest.attributes("paperweight-mappings-namespace" to "mojang")
    }
}

tasks.shadowJar {
    archiveBaseName.set("RayTraceAntiXray")
    archiveClassifier.set("")

    mergeServiceFiles()
    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    dependsOn(":paper_1_21_11:jar", ":paper_26_1_2:jar", ":paper_26_2:jar")
    from(project(":paper_1_21_11").tasks.jar.map { zipTree(it.archiveFile) })
    from(project(":paper_26_1_2").tasks.jar.map { zipTree(it.archiveFile) })
    from(project(":paper_26_2").tasks.jar.map { zipTree(it.archiveFile) })

    dependencies {
        include(dependency("org.bstats:bstats-bukkit:3.2.1"))
        include(dependency("org.bstats:bstats-base:3.2.1"))
        include(dependency("dev.faststats.metrics:bukkit:0.27.1"))
        include(dependency("dev.faststats.metrics:core:0.27.1"))
        include(dependency("dev.faststats.metrics:config:0.27.1"))
    }

    relocate("org.bstats", project.group.toString())
    relocate("dev.faststats", project.group.toString())

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.runServer {
    minecraftVersion("26.1.2")
    pluginJars.from(tasks.shadowJar.flatMap { it.archiveFile })
}

tasks.register<RunServer>("run1_21_11") {
    group = "runpaper"
    description = "Run a Paper 1.21.11 test server with the plugin"
    minecraftVersion("1.21.11")
    pluginJars.from(tasks.shadowJar.flatMap { it.archiveFile })
    runDirectory = layout.projectDirectory.dir("run1_21_11")
    systemProperties["Paper.IgnoreJavaVersion"] = true
}

tasks.register<RunServer>("run26_1_2") {
    group = "runpaper"
    description = "Run a Paper 26.1.2 test server with the plugin"
    minecraftVersion("26.1.2")
    pluginJars.from(tasks.shadowJar.flatMap { it.archiveFile })
    runDirectory = layout.projectDirectory.dir("run26_1_2")
}

tasks.register<RunServer>("run26_2") {
    group = "runpaper"
    description = "Run a Paper 26.2 test server with the plugin"
    minecraftVersion("26.2")
    pluginJars.from(tasks.shadowJar.flatMap { it.archiveFile })
    runDirectory = layout.projectDirectory.dir("run26_2")
}
