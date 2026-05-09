pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    // Explicit toolchain repositories (Gradle 9+): fixes deprecation when JDK is auto-provisioned without repos.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "RayTraceAntiXray"
