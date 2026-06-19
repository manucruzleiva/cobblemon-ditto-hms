# CLAUDE.md — Cobblemon Ditto HMs

## What it is

**Cobblemon Ditto HMs** is a multiloader (Fabric + NeoForge) Cobblemon mod for MC 1.21.1
that brings Ditto's 15 Pokopia HM abilities into the game. Abilities unlock by having the
required Pokémon in the player's Cobblemon party, and cost hunger on use.

- Mod ID: `cobblemon_ditto_hms`
- Author: manu
- GitHub: https://github.com/manucruzleiva/cobblemon-ditto-hms
- Modrinth: `cobblemon-ditto-hms` (slug — project must be created manually)

## Abilities (15 total)

| ID | Display | Unlock Pokémon | Effect | Hunger | CD (ticks) |
|---|---|---|---|---|---|
| water_gun | Water Gun | Squirtle | Water + in front; extinguish fire | 1 | 40 |
| leafage | Leafage | Bulbasaur | Grass on dirt; bone-meal crops in 5×5 | 1 | 40 |
| cut | Cut | Scyther | Chop logs/leaves 3-wide × 4-deep × 4-tall | 2 | 60 |
| rock_smash | Rock Smash | Hitmonchan | Haste II × 10 s | 2 | 100 |
| rototiller | Rototiller | Drilbur | Till 3×3 soil | 1 | 40 |
| jump | Jump | Magikarp | Jump Boost III × 15 s | 2 | 120 |
| surf | Surf | Lapras | Dolphin's Grace + Speed II × 30 s | 3 | 120 |
| camouflage | Camouflage | Zorua | Invisibility × 30 s | 3 | 200 |
| stockpile_water | Stockpile Water | Wooper | Place water in radius | 2 | 60 |
| strength | Strength | Machoke | Strength II × 15 s | 2 | 120 |
| rollout | Rollout | Graveler | Speed III + Haste II × 10 s | 3 | 160 |
| glide | Glide | Dragonite | Slow Falling × 60 s | 2 | 120 |
| waterfall | Waterfall | Gyarados | Levitation II burst × 3 s | 2 | 120 |
| magnet_rise | Magnet Rise | Magnemite | Levitation I × 30 s | 3 | 160 |
| dive | Dive | Vaporeon | Water Breathing + Dolphin's Grace × 60 s | 3 | 120 |

## Stack and versions

From [gradle.properties](gradle.properties):

| Component | Version |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader | 0.16.5 |
| Fabric API | 0.115.1+1.21.1 |
| Fabric Language Kotlin | 1.13.4+kotlin.2.2.0 |
| Kotlin | 2.2.0 |
| Cobblemon | 1.7.3+1.21.1 |
| Architectury Plugin | 3.4-SNAPSHOT |
| Architectury Loom | 1.10-SNAPSHOT |
| Architectury API | 13.0.8 |
| NeoForge | 21.1.93 |
| KotlinForForge (NeoForge) | 5.5.0 |

## How to build

No `gradlew` in the repo — use the cached Gradle distribution:

```powershell
$g = (Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists\gradle-9.2.0-bin\*\gradle-9.2.0\bin\gradle.bat").FullName
& $g --project-dir "C:\Users\shier\cobblemon-ditto-hms" clean build --console=plain
```

`clean build` is required — incremental can skip Kotlin recompilation.

**Auto-deploy:** the `copyToTestEnv` task (finalizedBy build) copies to:
- Fabric → `Cobblemon Official Modpack [Fabric]/mods/`
- NeoForge → `Cobblemon Official Modpack [NeoForge]/mods/`

## Project structure

```
common/src/main/kotlin/com/shier/dittohms/
  DittoHMs.kt              # Architectury event registration (tick + command)
  DittoHMsCommon.kt        # Platform-agnostic entrypoint (calls DittoHMs.init())
  ability/
    DittoAbility.kt        # Enum of all 15 abilities (id, unlockSpecies, hungerCost, cooldown)
    AbilityManager.kt      # unlock check, cooldown, hunger drain, handler dispatch
    handlers/              # One object per ability — execute(ServerPlayer): Boolean
  command/
    DittoHMsCommand.kt     # /dittohms use <ability> | /dittohms list
fabric/src/main/kotlin/com/shier/dittohms/fabric/
  DittoHMsFabric.kt        # ModInitializer → DittoHMsCommon.init()
neoforge/src/main/kotlin/com/shier/dittohms/neoforge/
  DittoHMsNeoForge.kt      # @Mod → DittoHMsCommon.init()
```

## Key Architectury Loom NeoForge gotcha (fixed)

Architectury Loom 1.10 with NeoForge requires:
1. `neoforge/gradle.properties` must contain `loom.platform=neoforge`
2. The NeoForge dependency in Kotlin DSL must use string notation: `"neoForge"("net.neoforged:neoforge:...")`
   (The `neoForge(...)` function call is not available in Kotlin DSL, only Groovy)

## Commands

- `/dittohms use <ability>` — use an ability (checks party, hunger, cooldown)
- `/dittohms list` — shows all abilities with unlock status for the player

## Unlock mechanic

`AbilityManager.isUnlocked()` checks `Cobblemon.storage.getParty(player)` for the species
using `pokemon.species.resourceIdentifier.path` (e.g. `"squirtle"`) as the identifier.
No friendship threshold is required — just having the Pokémon in party is enough.

## Hunger mechanic

Abilities call `player.foodData.addExhaustion(hungerCost * 4.0f)`. This burns saturation
first, then food level — same mechanic as running/fighting. If food level < hungerCost, the
ability is blocked.

## Modrinth publishing

Set the `MODRINTH_TOKEN` environment variable and run `build`. Minotaur is in the `fabric`
module and uploads both jars (Fabric + NeoForge) as one version. The project must first
be created manually on Modrinth with slug `cobblemon-ditto-hms`. To dry-run:
```powershell
& $g --project-dir "C:\Users\shier\cobblemon-ditto-hms" :fabric:modrinth -PmodrinthDebug
```

## Versioning

`modVersion` in [gradle.properties](gradle.properties). Bump before any Modrinth publish;
document in [CHANGELOG.md](CHANGELOG.md).
