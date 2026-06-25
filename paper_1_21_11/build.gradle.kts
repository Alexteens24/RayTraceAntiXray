import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    `my-conventions`
    id("io.papermc.paperweight.userdev")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    compileOnly(project(":"))
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
