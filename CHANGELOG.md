# Changelog

All notable changes are documented here.
Format: [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) + SemVer.

## [1.1.0] - 2026-06-21

### Added
- **Burning Bulwark** — 10th toggle HM (learn from a **Gouging Fire** with a **Blaze Rod**).
  Grants fire immunity and a thorns aura that scorches hostile mobs that get close. Blocks 4 hunger.
- **Per-toggle HUD icons** — each enabled toggle shows its own HM status-effect icon, so it's
  obvious which toggles are on. Underlying vanilla effects have their icons hidden.
- **Particles & sounds** for every HM use and toggle on/off.
- HM Discs now use **custom TM-style disc art** (gold "HM" rim + type-coloured ring) with the
  trigger item as the centre symbol.

### Changed
- **Fly** no longer equips an Elytra — it requires **Glide** to be learned, auto-enables Glide,
  and launches you skyward (re-use mid-air for a firework-style boost).
- **Glide** now uses real Elytra gliding (no more Slow Falling).
- **Mean Look** now makes hostile mobs **flee with pathfinding** (like creepers from a cat);
  it never affects Pokémon, friendly mobs, or players.
- **Rock Climb** reworked — look at a wall to climb, sneak to descend.
- **Cut** now also fells vines.
- **Dive** sink tuned so it no longer fights swimming.
- **HM Case** — disc icons, drag-and-drop reordering, sections separated without spacer panes,
  and ability descriptions shown in tooltips.
- Rewrote all ability descriptions to say what each HM does (no Minecraft buff names).

### Fixed
- Corrected several wrong ability descriptions (Leafage, Rock Smash, Rototiller, Waterfall,
  Dig, Defog, Revival Blessing, Strength).
- Fixed missing disc overlay textures for block/shield trigger items (Sunny Day, Harden, etc.).

## [1.0.0] - 2026-06-20

Initial release. **33 Pokopia-inspired HM abilities** (24 active + 9 toggles) for Cobblemon,
multiloader (Fabric + NeoForge).

### Core systems
- **HM Discs** — learn an ability by right-clicking its disc; the disc remains as a reusable badge.
- **Acquisition** — obtain a disc by holding an ability's trigger item and **sneak + right-clicking**
  one of its unlock Pokémon. Which Pokémon/item unlocks each HM is intentionally **not shown
  in-game** — it's documented only in the wiki.
- **HM Case** — crafteable hotbar item; right-click uses the active HM, shift-right-click opens a
  management GUI. Slots use each ability's trigger item as an icon; **shift+click reorders** HMs
  within a section.
- **Hunger** — active HMs cost hunger on use; toggles block food points from your max while enabled.
- **Per-ability config** — hunger / cooldown / power / hungerBlock via `/dittohm config` and the
  Cloth Config screen (Fabric, through Mod Menu).

### Active HMs (24)
Water Gun, Leafage, Cut, Rock Smash, Rototiller, Camouflage, Strength, Waterfall, Magnet Rise,
Ember, Bullet Seed, Teleport, Fly, Rain Dance, Sunny Day, Rest, Dig, Explosion, Thunder,
String Shot, Defog, Crabhammer, Revival Blessing, Charm.

- **Camouflage** morphs you into the block or entity in your crosshair (5 min).
- **Magnet Rise** is airborne-only and grants Levitation II + Slow Falling for 5s.
- **Charm** makes a targeted mob follow you for 2 minutes.

### Toggle HMs (9)
Jump (Jump Boost IV), Surf (Dolphin's Grace), Rollout (Speed), Dive (Water Breathing + sink),
Flash (Night Vision), Rock Climb (all blocks act as ladders), Mean Look (repel mobs),
Harden (full diamond armour, blocks 3 hunger), Glide (elytra-like glide physics, no item).

### Commands
`/dittohm use|list|select`, plus operator `give|config|reset_all|learn_all|forget|forget_all`.

### Multiloader
Fabric and NeoForge shipped as one Modrinth version.
