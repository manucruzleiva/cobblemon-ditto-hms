plugins {
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.10-SNAPSHOT" apply false
    kotlin("jvm") version "2.2.0" apply false
}

val minecraftVersion: String by project
val modVersion: String by project
val mavenGroup: String by project

architectury {
    minecraft = minecraftVersion
}

allprojects {
    group = mavenGroup
    version = modVersion
}

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    extensions.configure<org.gradle.api.plugins.BasePluginExtension> {
        archivesName.set("cobblemon-ditto-hms-${project.name}")
    }

    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.architectury.dev/")
        maven("https://thedarkcolour.github.io/KotlinForForge/")
        maven("https://maven.impactdev.net/repository/development/")
        maven("https://maven.nucleoid.xyz/")
        maven("https://cursemaven.com") { content { includeGroup("curse.maven") } }
    }

    val loom = extensions.getByType<net.fabricmc.loom.api.LoomGradleExtensionAPI>()
    dependencies {
        "minecraft"("com.mojang:minecraft:$minecraftVersion")
        "mappings"(loom.officialMojangMappings())
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        withSourcesJar()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(21)
    }
}
