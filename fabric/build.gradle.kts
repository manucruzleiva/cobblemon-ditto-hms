import java.io.File
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import net.darkhax.curseforgegradle.TaskPublishCurseForge

plugins {
    id("com.gradleup.shadow") version "8.3.5"
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

// ── Modrinth — uploads ONE version per loader. Modrinth Content Rules §5.7 require a
// single primary file per version (one per MC+loader); bundling both loaders' jars on one
// version as "additional files" gets the project rejected. So we POST two versions (one
// Fabric, one NeoForge) to the API directly. Requires MODRINTH_TOKEN in the environment.
fun jsonEscape(s: String): String = buildString {
    for (c in s) when (c) {
        '\\' -> append("\\\\")
        '"'  -> append("\\\"")
        '\n' -> append("\\n")
        '\r' -> append("\\r")
        '\t' -> append("\\t")
        else -> append(c)
    }
}

fun uploadModrinthVersion(token: String, jar: File, loader: String, versionNumber: String, displayName: String, changelog: String, mcVersion: String) {
    val projectId   = "JNfSyMuQ"   // Cobblemon Ditto HMs
    val cobblemonId = "MdwFAVRL"   // required dependency
    val data = "{\"name\":\"${jsonEscape(displayName)}\",\"version_number\":\"${jsonEscape(versionNumber)}\"," +
        "\"changelog\":\"${jsonEscape(changelog)}\"," +
        "\"dependencies\":[{\"project_id\":\"$cobblemonId\",\"dependency_type\":\"required\"}]," +
        "\"game_versions\":[\"$mcVersion\"],\"version_type\":\"release\",\"loaders\":[\"$loader\"]," +
        "\"featured\":true,\"project_id\":\"$projectId\",\"file_parts\":[\"file\"],\"primary_file\":\"file\"}"
    val boundary = "DittoHMsBoundary${System.currentTimeMillis()}"
    val out = ByteArrayOutputStream()
    fun w(s: String) = out.write(s.toByteArray(Charsets.UTF_8))
    w("--$boundary\r\nContent-Disposition: form-data; name=\"data\"\r\nContent-Type: application/json\r\n\r\n")
    w(data); w("\r\n")
    w("--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"${jar.name}\"\r\nContent-Type: application/java-archive\r\n\r\n")
    out.write(jar.readBytes()); w("\r\n")
    w("--$boundary--\r\n")
    val req = HttpRequest.newBuilder()
        .uri(URI.create("https://api.modrinth.com/v2/version"))
        .header("Authorization", token)
        .header("User-Agent", "manucruzleiva/cobblemon-ditto-hms (gradle publish)")
        .header("Content-Type", "multipart/form-data; boundary=$boundary")
        .POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray()))
        .build()
    val resp = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString())
    if (resp.statusCode() !in 200..299)
        throw GradleException("Modrinth upload failed for $loader ($versionNumber): HTTP ${resp.statusCode()} ${resp.body()}")
    println("Modrinth: uploaded $versionNumber [$loader]")
}

val publishModrinth by tasks.registering {
    group = "publishing"
    description = "Upload one Modrinth version per loader (Fabric + NeoForge)."
    dependsOn("remapJar", ":neoforge:remapJar")
    onlyIf { !System.getenv("MODRINTH_TOKEN").isNullOrEmpty() }
    doLast {
        val token     = System.getenv("MODRINTH_TOKEN")!!
        val changelog = latestChangelog()
        val fabricJar = tasks.named("remapJar").get().outputs.files.singleFile
        val neoJar    = project(":neoforge").tasks.named("remapJar").get().outputs.files.singleFile
        uploadModrinthVersion(token, fabricJar, "fabric",   "$modVersion+fabric",   "$modVersion (Fabric)",   changelog, minecraftVersion)
        uploadModrinthVersion(token, neoJar,    "neoforge", "$modVersion+neoforge", "$modVersion (NeoForge)", changelog, minecraftVersion)
    }
}
tasks.named("build") { finalizedBy(publishModrinth) }

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
