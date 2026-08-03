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
    paperweight.paperDevBundle("26.1.2.build.65-stable")
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks.withType<JavaCompile>().configureEach {

    options.release.set(25)
}
