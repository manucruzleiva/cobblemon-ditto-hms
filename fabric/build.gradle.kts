import java.io.File
import net.darkhax.curseforgegradle.TaskPublishCurseForge

plugins {
    id("com.gradleup.shadow") version "8.3.5"
    id("com.modrinth.minotaur") version "2.8.7"
    id("net.darkhax.curseforgegradle") version "1.1.26"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val loaderVersion: String by project
val fabricVersion: String by project
val fabricKotlinVersion: String by project
val architecturyApiVersion: String by project
val minecraftVersion: String by project
val modVersion: String by project
val clothConfigVersion: String by project
val modMenuVersion: String by project

val common: Configuration by configurations.creating
val shadowBundle: Configuration by configurations.creating

configurations {
    compileClasspath.get().extendsFrom(common)
    runtimeClasspath.get().extendsFrom(common)
    getByName("developmentFabric").extendsFrom(common)
}

repositories {
    maven { url = uri("https://maven.shedaniel.me/") }
    maven { url = uri("https://maven.terraformersmc.com/releases") }
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modImplementation("com.terraformersmc:modmenu:$modMenuVersion")

    common(project(":common", configuration = "namedElements")) { isTransitive = false }
    shadowBundle(project(":common", configuration = "transformProductionFabric"))
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") { expand("version" to project.version) }
    }
    shadowJar {
        configurations = listOf(shadowBundle)
        archiveClassifier.set("dev-shadow")
        // Shadow plugin doesn't auto-pick up Kotlin output in Loom — include explicitly
        from(project.sourceSets["main"].output)
    }
    remapJar {
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        dependsOn(shadowJar)
        archiveClassifier.set(null as String?)
    }
}

fun latestChangelog(): String {
    val f = rootProject.file("CHANGELOG.md")
    if (!f.exists()) return "See CHANGELOG.md"
    val match = Regex("(?ms)^## \\[\\d+\\.\\d+\\.\\d+\\][^\\n]*\\n(.*?)(?=^## \\[)").find(f.readText())
    val body = match?.groupValues?.get(1)?.trim()
    return if (body.isNullOrEmpty()) "See CHANGELOG.md" else body
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("cobblemon-ditto-hms")
    versionNumber.set(modVersion)
    versionName.set("Cobblemon Ditto HMs $modVersion")
    versionType.set("release")
    uploadFile.set(tasks.named("remapJar"))
    additionalFiles.add(project(":neoforge").tasks.named("remapJar"))
    gameVersions.add(minecraftVersion)
    loaders.addAll("fabric", "neoforge")
    changelog.set(latestChangelog())
    failSilently.set(true)
    debugMode.set(project.hasProperty("modrinthDebug"))
    dependencies {
        required.project("cobblemon")
    }
}

tasks.named("modrinth") {
    onlyIf { System.getenv("MODRINTH_TOKEN") != null }
    dependsOn(":neoforge:remapJar")
}
tasks.named("build") { finalizedBy("modrinth") }

// ── CurseForge (project 1583189) — uploads both jars as one release ──────────────
// Requires CURSEFORGE_TOKEN in the environment. Game-version / modloader IDs are
// resolved by name against the CurseForge API at publish time.
val curseForgeProjectId = "1583189"
val publishCurseForge by tasks.registering(TaskPublishCurseForge::class) {
    apiToken = System.getenv("CURSEFORGE_TOKEN") ?: ""

    val fabricFile = upload(curseForgeProjectId, tasks.named("remapJar"))
    fabricFile.releaseType = "release"
    fabricFile.changelogType = "markdown"
    fabricFile.changelog = latestChangelog()
    fabricFile.displayName = "Cobblemon Ditto HMs $modVersion (Fabric)"
    fabricFile.addGameVersion(minecraftVersion)
    fabricFile.addModLoader("Fabric")
    fabricFile.addRequirement("cobblemon")

    val neoFile = upload(curseForgeProjectId, project(":neoforge").tasks.named("remapJar"))
    neoFile.releaseType = "release"
    neoFile.changelogType = "markdown"
    neoFile.changelog = latestChangelog()
    neoFile.displayName = "Cobblemon Ditto HMs $modVersion (NeoForge)"
    neoFile.addGameVersion(minecraftVersion)
    neoFile.addModLoader("NeoForge")
    neoFile.addRequirement("cobblemon")

    dependsOn("remapJar", ":neoforge:remapJar")
    onlyIf { !System.getenv("CURSEFORGE_TOKEN").isNullOrEmpty() }
}
tasks.named("build") { finalizedBy(publishCurseForge) }

val testEnvDir: String? by project
if (testEnvDir != null) {
    val copyToTestEnv = tasks.register<Copy>("copyToTestEnv") {
        from(tasks.named("remapJar"))
        into("$testEnvDir/mods")
        doFirst {
            file("$testEnvDir/mods")
                .listFiles { f -> f.name.startsWith("cobblemon-ditto-hms-") && f.extension == "jar" }
                ?.forEach { it.delete() }
        }
        onlyIf { file("$testEnvDir/mods").isDirectory }
        doLast { println("copyToTestEnv(fabric): deployed to $testEnvDir/mods") }
    }
    tasks.named("build") { finalizedBy(copyToTestEnv) }
}
