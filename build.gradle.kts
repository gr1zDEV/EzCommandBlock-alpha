plugins {
    java
}

group = "com.ezinnovations"
version = project.findProperty("version")?.toString() ?: "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    implementation("org.yaml:snakeyaml:2.2")
}

tasks.processResources {
    filesMatching(listOf("plugin.yml", "velocity-plugin.json")) {
        expand("version" to project.version)
    }
}

val paperJar by tasks.registering(Jar::class) {
    group = "build"
    description = "Builds the Paper/Folia plugin JAR."

    archiveBaseName.set("EzCommandBlocker-paper")
    from(sourceSets.main.get().output)
    include("com/ezinnovations/ezcommandblocker/**")
    exclude("com/ezinnovations/ezcommandblocker/velocity/**")
    include("plugin.yml")
    include("config.yml")
    exclude("velocity-plugin.json")
}

val velocityJar by tasks.registering(Jar::class) {
    group = "build"
    description = "Builds the Velocity plugin JAR."

    archiveBaseName.set("EzCommandBlocker-velocity")
    from(sourceSets.main.get().output)
    include("com/ezinnovations/ezcommandblocker/velocity/**")
    include("velocity-plugin.json")
    include("velocity-config.yml")
    exclude("plugin.yml")
    exclude("config.yml")
}

tasks.jar {
    enabled = false
}

tasks.assemble {
    dependsOn(paperJar, velocityJar)
}
