architectury {
    common("fabric", "neoforge")
}

val architecturyApiVersion: String by project
val loaderVersion: String by project
val fabricKotlinVersion: String by project
val cobblemonVersion: String by project

dependencies {
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")
    modImplementation("dev.architectury:architectury:$architecturyApiVersion")
    // Cobblemon: common compiles against the Fabric build; same mojmapped API on NeoForge at runtime.
    modImplementation("com.cobblemon:fabric:$cobblemonVersion")
}
