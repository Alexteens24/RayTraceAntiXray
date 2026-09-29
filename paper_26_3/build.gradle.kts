import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    `my-conventions`
    id("io.papermc.paperweight.userdev")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

dependencies {
    compileOnly(project(":"))
    paperweight.paperDevBundle("26.3.build.135-beta")
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}
