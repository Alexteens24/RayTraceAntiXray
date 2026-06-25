pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
    plugins {
        id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    }
}

plugins {
    // Explicit toolchain repositories (Gradle 9+): fixes deprecation when JDK is auto-provisioned without repos.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "RayTraceAntiXray"

include("paper_1_21_11", "paper_26_1_2")
