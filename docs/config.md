# Configuration

Every ability has three tunable values:

| Parameter | Description |
|---|---|
| **Hunger** | Food levels required and consumed per use (0 = free) |
| **Cooldown** | Ticks between uses (20 ticks = 1 second; 0 = no cooldown) |
| **Power** | Ability-specific: radius, duration, damage, count, etc. |

---

## Cloth Config GUI (Fabric)

Open **Mod Menu → Cobblemon Ditto HMs → ⚙** to see sliders for every ability's three values.  
Changes save automatically.

---

## In-game commands

Operator-only (`permission level 2`):

```
/dittohms config <ability> hunger <0–20>
/dittohms config <ability> cooldown <0–24000>
/dittohms config <ability> power <0–512>
/dittohms config <ability> reset
/dittohms config reset_all
```

---

## Config file

The file is saved at:

- **Fabric:** `config/cobblemon_ditto_hms.json`
- **NeoForge:** `config/cobblemon_ditto_hms.json`

You can edit it directly — changes take effect on next server start.

---

## Default values

| Ability | Hunger | Cooldown | Power |
|---|---|---|---|
| Water Gun | 1 | 2s | radius 3 |
| Leafage | 1 | 2s | radius 5 |
| Cut | 2 | none | max 128 blocks |
| Rock Smash | 2 | none | max 32 ores |
| Rototiller | 1 | none | 1 block |
| Camouflage | 3 | 10s | 30s duration |
| Stockpile Water | 2 | 3s | 1 block |
| Strength | 2 | none | 1 block push |
| Waterfall | 2 | 6s | 2s levitation |
| Magnet Rise | 3 | 8s | 60s hover |
| Ember | 1 | 1s | reach 5 blocks |
| Bullet Seed | 2 | 1s | 5 seeds/burst |
| Teleport | 5 | 5s | max 30 blocks |
| Fly | 3 | none | 60s flight |
| Rain Dance | 3 | 10s | 5 min rain |
| Sunny Day | 3 | 10s | 5 min clear |
| Rock Climb | 2 | 5s | 15s effects |
| Rest | 0* | 10 min | — |
| Dig | 2 | none | Haste III |
| Explosion | 15 | 10s | blast 4 |
| Thunder | 3 | 5s | 3 bolts |
| String Shot | 1 | 2s | radius 1 |
| Defog | 2 | 5s | radius 10 |
| Crab Hammer | 3 | 2s | — |
| Jump *(toggle)* | — | — | Jump Boost II |
| Surf *(toggle)* | — | — | Dolphin's Grace |
| Glide *(toggle)* | — | — | Slow Falling |
| Rollout *(toggle)* | — | — | Speed I |
| Dive *(toggle)* | — | — | Water Breathing |
| Flash *(toggle)* | — | — | Night Vision |

\* Rest sets hunger to 1 regardless of cost.
