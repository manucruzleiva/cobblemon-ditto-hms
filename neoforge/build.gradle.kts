plugins {
    id("com.gradleup.shadow") version "8.3.5"
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

val neoforgeVersion: String by project
val kotlinForForgeVersion: String by project
val architecturyApiVersion: String by project

val common: Configuration by configurations.creating
val shadowBundle: Configuration by configurations.creating

configurations {
    compileClasspath.get().extendsFrom(common)
    runtimeClasspath.get().extendsFrom(common)
    getByName("developmentNeoForge").extendsFrom(common)
}

dependencies {
    neoForge("net.neoforged:neoforge:$neoforgeVersion")
    implementation("thedarkcolour:kotlinforforge-neoforge:$kotlinForForgeVersion")
    modImplementation("dev.architectury:architectury-neoforge:$architecturyApiVersion")

    common(project(":common", configuration = "namedElements")) { isTransitive = false }
    shadowBundle(project(":common", configuration = "transformProductionNeoForge")) { isTransitive = false }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("META-INF/neoforge.mods.toml") { expand("version" to project.version) }
    }
    shadowJar {
        configurations = listOf(shadowBundle)
        archiveClassifier.set("dev-shadow")
    }
    remapJar {
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        dependsOn(shadowJar)
        archiveClassifier.set(null as String?)
    }
}

val testEnvDirNeoForge: String? by project
if (testEnvDirNeoForge != null) {
    val copyToTestEnv = tasks.register<Copy>("copyToTestEnv") {
        from(tasks.named("remapJar"))
        into("$testEnvDirNeoForge/mods")
        doFirst {
            file("$testEnvDirNeoForge/mods")
                .listFiles { f -> f.name.startsWith("cobblemon-ditto-hms-") && f.extension == "jar" }
                ?.forEach { it.delete() }
        }
        onlyIf { file("$testEnvDirNeoForge/mods").isDirectory }
        doLast { println("copyToTestEnv(neoforge): deployed to $testEnvDirNeoForge/mods") }
    }
    tasks.named("build") { finalizedBy(copyToTestEnv) }
}
