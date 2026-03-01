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
    filesMatching(listOf("plugin.yml", "META-INF/velocity-plugin.json")) {
        expand("version" to project.version)
    }
}

tasks.jar {
    archiveBaseName.set("EzCommandBlocker")
}
