# CLAUDE.md — Cobblemon Ditto HMs

## What it is

**Cobblemon Ditto HMs** is a multiloader (Fabric + NeoForge) Cobblemon mod for MC 1.21.1
that brings Ditto's Pokopia-inspired HM abilities into the game. There are **34 abilities**
(24 active + 10 toggles). Abilities are learned from **HM Discs**, which are obtained by
sneak-right-clicking a specific Pokémon while holding that ability's trigger item.

- Mod ID: `cobblemon_ditto_hms`
- Author: manu
- GitHub: https://github.com/manucruzleiva/cobblemon-ditto-hms
- Modrinth: `cobblemon-ditto-hms` (slug — project must be created manually)

> **Design note:** which Pokémon teaches each HM is intentionally **not shown in-game**
> (tooltips, GUI). That information lives only in the wiki (`docs/`) — discovery is part of
> the experience. Keep the in-game text free of species hints; put the acquisition tables in
> the wiki.

## Abilities (34 total)

`hunger` = food cost per use (active) or food points blocked from max (toggle, the
`hungerBlock` field). `cd` = cooldown ticks (20t = 1s). `power` is ability-specific.

### Active HMs (24) — used on right-click; cost hunger + start a cooldown

| ID | Display | Trigger item | Effect | hunger | cd | power |
|---|---|---|---|---|---|---|
| water_gun | Water Gun | Blue Dye | Water ahead; extinguish fire | 1 | 40 | 3 |
| leafage | Leafage | Dandelion | Instantly grow all nearby crops | 1 | 40 | 5 |
| cut | Cut | Stick | Treecapitator (BFS logs+leaves+vines) | 2 | 0 | 128 |
| rock_smash | Rock Smash | Flint | Break aimed block; vein-mine ores | 2 | 0 | 32 |
| rototiller | Rototiller | Coarse Dirt | Till aimed dirt block | 1 | 0 | 1 |
| camouflage | Camouflage | Ink Sac | Morph into aimed block/entity (5 min) | 3 | 200 | 6000 |
| strength | Strength | Iron Ingot | Push aimed block | 2 | 0 | 1 |
| waterfall | Waterfall | Prismarine Shard | **In water**: ride upward current to surface | 2 | 120 | 40 |
| magnet_rise | Magnet Rise | Iron Nugget | **Airborne-only**: Levitation II + Slow Fall 5s | 2 | 200 | 100 |
| ember | Ember | Blaze Powder | Flint & Steel behaviour | 1 | 20 | 5 |
| bullet_seed | Bullet Seed | Wheat Seeds | Seed barrage from inventory | 2 | 20 | 5 |
| teleport | Teleport | Ender Eye | Blink to aimed block | 5 | 100 | 30 |
| fly | Fly | Phantom Membrane | **Requires Glide learned**; launches you up + auto-enables Glide (no Elytra) | 3 | 20 | 1200 |
| rain_dance | Rain Dance | Prismarine Crystals | Rain 5 min | 5 | 200 | 6000 |
| sunny_day | Sunny Day | Sunflower | Clear weather 5 min | 5 | 200 | 6000 |
| rest | Rest | Apple | Full heal (multiplayer consensus) | 0 | 12000 | 0 |
| dig | Dig | Diamond Shovel | Haste III; instant-break aimed soil | 2 | 0 | 1 |
| explosion | Explosion | TNT | Big blast, survive at 1 HP | 15 | 200 | 4 |
| thunder | Thunder | Lightning Rod | 3 lightning bolts | 3 | 100 | 3 |
| string_shot | String Shot | String | Web trap around you | 1 | 40 | 1 |
| defog | Defog | Glass | Cure your own harmful status effects | 2 | 100 | 10 |
| crab_hammer | Crabhammer | Mace | Heavy AoE knockback hit | 3 | 40 | 1 |
| revival_blessing | Revival Blessing | Totem of Undying | Heal & revive whole Pokémon party | 1 | 24000 | 0 |
| charm | Charm | Pink Tulip | Charm aimed mob → follows 2 min | 2 | 60 | 2400 |

### Toggle HMs (10) — passive; enable/disable from the HM Case or `/dittohm use`

| ID | Display | Trigger item | Effect | hungerBlock | power |
|---|---|---|---|---|---|
| jump | Jump | Cod | Jump Boost (power = level; default IV) | 2 | 4 |
| surf | Surf | Kelp | Dolphin's Grace (always while on) | 2 | 0 |
| rollout | Rollout | Cobblestone | Speed (power = amplifier) | 2 | 0 |
| dive | Dive | Nautilus Shell | Water Breathing + sink underwater | 2 | 0 |
| flash | Flash | Glowstone Dust | Night Vision | 2 | 0 |
| rock_climb | Rock Climb | Chain | All blocks act as ladders (HUD icon only) | 2 | 0 |
| mean_look | Mean Look | Ominous Bottle | Repel nearby **hostile** mobs only — never Pokémon/friendly/players (power = radius) | 2 | 30 |
| harden | Harden | Shield | Full diamond armour via attributes | 3 | 0 |
| glide | Glide | Feather | Elytra-like glide physics (no item) | 2 | 0 |
| burning_bulwark | Burning Bulwark | Blaze Rod | Fire Resistance + thorns aura that scorches adjacent hostile mobs (power = thorns dmg) | 4 | 2 |

> **Burning Bulwark** is learned from a **Gouging Fire** (`gougingfire`). Thorns is an
> aura: hostile mobs within ~1.6 blocks are ignited + take `power` damage twice a second
> (no vanilla Thorns mob-effect exists, so it's implemented server-side).

> **Fly** no longer equips an Elytra. It requires **Glide** to be learned, auto-enables the
> Glide toggle on use, and launches you upward; Glide's tick physics do the actual flying.

> **Toggle HUD:** every enabled toggle applies its own custom status effect
> (`HMEffects` / `HMIndicatorEffect`, id `hm_<ability>`), so a distinct HM icon shows in the
> vanilla status-effect bar. The real gameplay effects (jump, speed, etc.) are applied with
> their own icons hidden so only the HM icons show.

> **Disc art:** HM discs use custom TM-style base textures (`textures/item/hm_disc_active`
> for the 24 actives, `hm_disc_toggle` for the 10 toggles) — a gold "HM" rim around a
> type-coloured ring — with the trigger item kept as the centre overlay (layer1). Effect
> icons live in `textures/mob_effect/hm_<ability>.png`.

> **Descriptions:** `DittoAbility.description` is player-facing — it must say *what the HM
> does and what it's for*, never naming Minecraft effects (no "Haste", "Night Vision",
> "Dolphin's Grace", etc.). Verify new/changed descriptions against the actual handler.

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
| Architectury API | 13.0.8 |
| NeoForge | 21.1.x |
| KotlinForForge (NeoForge) | 5.5.0 |
| Cloth Config | 15.0.127 |
| Mod Menu | 11.0.2 |

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
    DittoAbility.kt        # Enum of all abilities (id, displayName, unlockPool,
                           #   triggerItem, isPassive, description)
    AbilityManager.kt      # learn check, cooldown, hunger, passive tick, handler dispatch
    handlers/              # One object per active ability — execute(ServerPlayer): Boolean
                           #   (some also have a tick(MinecraftServer) for ongoing state)
  command/
    DittoHMsCommand.kt     # /dittohm use|list|select|give|config|learn_all|forget|forget_all
  config/
    HMsConfig.kt           # Per-ability JSON config (hunger/cooldown/power/hungerBlock)
  item/
    HMDiscItem.kt          # Learnable disc; right-click learns then uses
    HMCaseItem.kt          # Hotbar manager item; right-click=use active, shift=open GUI
    HMCaseMenu.kt          # Chest GUI: 24 active + 9 toggle slots, shift-click reorder
    HMItems.kt             # Platform-agnostic item registry
  storage/
    HMsSavedData.kt        # World SavedData: per-player learned + passive-enabled bitmasks
  interaction/
    HMInteractionHandler.kt# Sneak+right-click a Pokémon w/ trigger item → grants HM Disc
fabric/src/main/kotlin/com/shier/dittohms/fabric/
  DittoHMsFabric.kt        # ModInitializer → registers items/tab/commands/tick/interaction
neoforge/src/main/kotlin/com/shier/dittohms/neoforge/
  DittoHMsNeoForge.kt      # @Mod → DeferredRegister items/tab + native events
```

## Key mechanics

### Learning & using

- **Acquire a disc:** hold the ability's trigger item, sneak+right-click a Pokémon in its
  `unlockPool` ([HMInteractionHandler](common/src/main/kotlin/com/shier/dittohms/interaction/HMInteractionHandler.kt)).
- **Learn:** right-click the disc once → learned permanently (disc consumed unless in creative).
  Passives are **not** auto-enabled — the player enables them from the HM Case.
- **Use:** right-click a learned disc, or set it active in the HM Case and right-click the Case.

### HM Case GUI ([HMCaseMenu.kt](common/src/main/kotlin/com/shier/dittohms/item/HMCaseMenu.kt))

- 6-row chest. Active section = slots 9–32 (24), toggle section = slots 36–44 (9).
- Each slot shows the ability's **trigger item** as its icon (so HMs are easy to tell apart).
- Discs/case items use layered models (`music_disc_13`/`music_disc_mellohi` + trigger overlay).
- **Shift+click reorders:** shift+click selects a slot, shift+click another in the same
  section swaps them. Ordering is per-GUI-session (resets on reopen).
- Unlearned slots show grey glass with **no species hint** (wiki-only secret).

### Hunger ([AbilityManager.tick](common/src/main/kotlin/com/shier/dittohms/ability/AbilityManager.kt))

- Active: `player.foodData.addExhaustion(hungerCost * 4.0f)`; blocked if foodLevel < cost.
- Toggle: each enabled toggle blocks `hungerBlock` food points from max (cap = 20 − Σblocked,
  floor 2). At the cap a slow Regeneration keeps health topped up.

### Passives ([AbilityManager.applyPassive](common/src/main/kotlin/com/shier/dittohms/ability/AbilityManager.kt))

Re-applied each tick while enabled; `refreshAmbient()` only re-adds an effect when missing /
expiring / wrong amplifier to avoid effect spam. ROCK_CLIMB and GLIDE physics run in `tick()`.

## NeoForge / Architectury gotchas

1. `neoforge/gradle.properties` must contain `loom.platform=neoforge`.
2. NeoForge dependency in Kotlin DSL uses string notation: `"neoForge"("net.neoforged:neoforge:...")`.
3. **Shadow + Loom:** the Fabric platform module's Kotlin output is **not** auto-included in the
   shadow jar. `fabric/build.gradle.kts` `shadowJar` must add `from(project.sourceSets["main"].output)`
   or `DittoHMsFabric` is `ClassNotFoundException` at runtime.
4. `Display.setTransformation` / `BlockDisplay` block-state are private in Loom mappings — set
   via reflection (see [CamouflageHandler.kt](common/src/main/kotlin/com/shier/dittohms/ability/handlers/CamouflageHandler.kt)).
5. Don't mutate a map while iterating it. CamouflageHandler/CharmHandler use a pure
   `cleanupEffects()` helper + `iter.remove()` to avoid `ConcurrentModificationException`.

## Commands

- `/dittohm use <ability>` — use/toggle an ability
- `/dittohm list` — list all abilities with learn status & config
- `/dittohm select <ability>` — set active ability in the held HM Case
- `/dittohm give <ability> <player>` *(op)* — give an HM Disc
- `/dittohm config <ability> hunger|cooldown|power|hungerblock <v>` *(op)*
- `/dittohm config reset_all` *(op)* — restore defaults (do this after enum changes)
- `/dittohm learn_all [player]` *(op)* — learn everything (does not auto-enable toggles)
- `/dittohm forget <ability> [player]` / `forget_all [player]` *(op)*

## Save-data caveat

Learned/enabled state is stored as **bitmasks keyed by enum ordinal** in
[HMsSavedData.kt](common/src/main/kotlin/com/shier/dittohms/storage/HMsSavedData.kt). Adding,
removing, or reordering entries in `DittoAbility` shifts ordinals and scrambles existing save
data. After such changes during dev: wipe the world's saved data (or `/dittohm forget_all`
then `learn_all`) and run `/dittohm config reset_all`.

## Publishing

Tokens live in `.env` (gitignored). Both publishers are in the `fabric` module and upload both
jars (Fabric + NeoForge) as one release; each is gated `onlyIf` its token env-var is present, so
normal dev builds never publish. **Bump `modVersion` first** (re-uploading a version fails).

- **Modrinth** (custom `publishModrinth` task, slug `cobblemon-ditto-hms`, id `JNfSyMuQ`): set
  `MODRINTH_TOKEN`, run `build` (or `:fabric:publishModrinth`). It POSTs **one version per loader**
  (`<ver>+fabric`, `<ver>+neoforge`) to the API — Modrinth Content Rules §5.7 forbid bundling both
  loaders' jars on one version as "additional files" (it gets rejected). §5.1 also requires
  accurate environment metadata (`client_side`/`server_side` = `required` here). First publish of a
  new project is a **draft** until *Submit for review* (Modrinth moderates it).
  > We dropped Minotaur because it only uploads a single combined version; the custom task does the
  > per-loader split. The Modrinth project itself was created via the API (`POST /v2/project`).
- **CurseForge** (CurseForgeGradle, project id `1587850` — must be a **Mod** project, not a
  Modpack, or `.jar` uploads fail "verify archive"): set `CURSEFORGE_TOKEN`, run `build` (or
  `:fabric:publishCurseForge`). Uploads the two jars as **separate files** (one per loader);
  game-version/modloader IDs resolve by name at publish time. CurseForge projects **cannot** be
  created via API — manual creation + staff approval on the website first.

## Versioning

`modVersion` in [gradle.properties](gradle.properties). Bump before any Modrinth publish;
document in [CHANGELOG.md](CHANGELOG.md).
