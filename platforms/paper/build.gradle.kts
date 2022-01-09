plugins {
    id("platform-conventions")
    id("io.papermc.paperweight.userdev") version "1.3.3"
    id("xyz.jpenilla.run-paper") version "1.0.6" // Adds runServer and runMojangMappedServer tasks for testing
}

repositories {
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")

    implementation(project(":common:common-core"))

    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.7") {
        exclude("org.yaml")
    }
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.7")
}

tasks {
    processResources {
        val replacements = mapOf(
            "version" to project.version
        )
        inputs.properties(replacements)

        filesMatching("plugin.yml") {
            expand(replacements)
        }
    }

    jar {
        archiveClassifier.set("deobf")
    }

    shadowJar {
        archiveClassifier.set("deobf-all")
    }

    reobfJar {
        inputJar.set(shadowJar.flatMap { it.archiveFile })
    }
}

otgPlatform {
    productionJar.set(tasks.reobfJar.flatMap { it.outputJar })
}
