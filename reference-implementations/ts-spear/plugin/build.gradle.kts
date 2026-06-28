plugins {
    id("com.gradleup.shadow") version "8.3.5"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":storage-sqlite"))
    implementation(project(":storage-jdbc"))
    implementation(project(":checks:movement"))
    implementation(project(":checks:combat"))
    implementation(project(":checks:networking"))

    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set("TSSpear")
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}