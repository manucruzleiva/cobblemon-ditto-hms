# Active HMs

Active HMs are used by **right-clicking** the HM Disc or by setting them as active in the HM Case.  
Each use costs hunger and starts a cooldown.

---

## Acquisition table

| HM | Pokémon | Trigger item |
|---|---|---|
| Water Gun | Squirtle | Blue Dye |
| Leafage | Bulbasaur | Dandelion |
| Cut | Scyther | Stick |
| Rock Smash | Hitmonchan | Flint |
| Rototiller | Drilbur | Coarse Dirt |
| Camouflage | Zorua | Ink Sac |
| Stockpile Water | Wooper | Clay Ball |
| Strength | Machoke | Iron Ingot |
| Waterfall | Gyarados | Prismarine Shard |
| Magnet Rise | Magnemite | Iron Nugget |
| Ember | Charmander | Blaze Powder |
| Bullet Seed | Seedot | Wheat Seeds |
| Teleport | Abra | Ender Eye |
| Fly | Pidgeot | Phantom Membrane |
| Rain Dance | Politoed | Prismarine Crystals |
| Sunny Day | Torkoal | Sunflower |
| Rock Climb | Rhydon | Chain |
| Rest | Snorlax | Apple |
| Dig | Diglett | Diamond Shovel |
| Explosion | Voltorb | TNT |
| Thunder | Pikachu | Lightning Rod |
| String Shot | Caterpie | String |
| Defog | Togekiss | Glass |
| Crab Hammer | Kingler | Mace |

---

## Ability details

### Water Gun
**Hunger:** 1 · **Cooldown:** 2s · **Power:** radius 3

Fires in the direction you're looking:

- Extinguishes fire blocks in a radius
- Converts a **Sponge** into a **Wet Sponge**
- Places a **Water** block on the aimed surface

---

### Leafage
**Hunger:** 1 · **Cooldown:** 2s · **Power:** radius 5

Fully grows all **crop blocks** (wheat, carrots, potatoes, beetroot, etc.) in a 5×5 area.  
Does **not** affect grass, saplings, or flowers — only `CropBlock` instances.

---

### Cut
**Hunger:** 2 · **Cooldown:** none · **Power:** max 128 blocks

Treecapitator using BFS:

- Aim at a **log** → cuts all connected logs (and leaves attached to them)
- Aim at a **leaf** → strips all connected leaves only

Drops match a Netherite Axe.

---

### Rock Smash
**Hunger:** 2 · **Cooldown:** none · **Power:** max 32 ores

Always grants **Haste II** for 10 s.  
If the aimed block is an **ore**, it vein-mines all connected ores of the same type (BFS).  
Any other **pickaxe-minable** block is broken immediately (1 block).

---

### Rototiller
**Hunger:** 1 · **Cooldown:** none

Tills the **single dirt-type block** you are aiming at into Farmland.  
Works on: Dirt, Grass Block, Coarse Dirt, Dirt Path.

---

### Camouflage
**Hunger:** 3 · **Cooldown:** 10s · **Power:** 600t (30s duration)

- Aim at a **block** → spawns a `BlockDisplay` entity showing that block at your position (follows you every tick).
- Aim at an **entity** → adjusts your scale to match the entity's size and copies its name tag.
- No invisibility — you are **visually** disguised but physically present.

---

### Stockpile Water
**Hunger:** 2 · **Cooldown:** 3s

Places **one Water source block** on the surface you are aiming at.

---

### Strength
**Hunger:** 2 · **Cooldown:** none

Pushes the **aimed block** one block in your horizontal facing direction.  
Cannot move: fluids, blocks with inventories (chests, etc.).

---

### Waterfall
**Hunger:** 2 · **Cooldown:** 6s · **Power:** 40t (2s)

**Must be used in water.** Gives a burst of Levitation II + a strong upward impulse, propelling you up through waterfall columns.

---

### Magnet Rise
**Hunger:** 3 · **Cooldown:** 8s · **Power:** 1200t (60s)

Grants **creative-mode flight** for 60 seconds with:

- **Slow Falling** — gentle, floaty descent
- **Resistance I** — electromagnetic steel durability

Feels like hovering on a Magnezone.

---

### Ember
**Hunger:** 1 · **Cooldown:** 1s · **Power:** reach 5 blocks

Works exactly like **Flint & Steel**:

- Places fire on the aimed block face
- Primes **TNT** instantly
- Sets entities in front of you on fire (5 s)

---

### Bullet Seed
**Hunger:** 2 · **Cooldown:** 1s · **Power:** 5 seeds per burst

Fires a **rapid barrage** of seed projectiles (one every 3 ticks ≈ 0.15 s).  
Consumes any item whose registry ID contains `"seed"` from your inventory.  
The firing direction is locked at the moment of activation.

---

### Teleport
**Hunger:** 5 · **Cooldown:** 5s · **Power:** max 30 blocks

Teleports you to the block you are looking at (up to 30 blocks away).  
Automatically finds the nearest safe landing position.  
Plays the Enderman teleport sound.

---

### Fly
**Hunger:** 3 · **Cooldown:** none · **Power:** 1200t (60s)

Grants **creative-mode flight** for 60 seconds with a **Speed II** boost while airborne.  
Feels like riding a Charizard or Pidgeot.

---

### Rain Dance
**Hunger:** 3 · **Cooldown:** 10s · **Power:** 6000t (5 min)

Activates **rain** on the overworld for 5 minutes.

---

### Sunny Day
**Hunger:** 3 · **Cooldown:** 10s · **Power:** 6000t (5 min)

Clears the weather to **sunny** for 5 minutes.

---

### Rock Climb
**Hunger:** 2 · **Cooldown:** 5s · **Power:** 300t (15s)

Applies for 15 seconds:

- **Jump Boost IV** — can reach ~5 extra blocks per jump
- **Slow Falling** — controlled descent
- **Speed II** — reach ledges quickly

---

### Rest
**Hunger:** none required · **Cooldown:** 10 min

- Restores **full HP** and clears all status effects
- Leaves hunger at **1** (heavy cost)
- Skips time: day → night, night → day

!!! warning "Multiplayer"
    In multiplayer, **all online players must agree** to rest within a 15-second window. Any player who hasn't used Rest yet will see a notification.

---

### Dig
**Hunger:** 2 · **Cooldown:** none · **Power:** Haste III

Grants **Haste III** for 15 s and **instantly breaks** the single shovel-minable block you are aiming at (dirt, sand, gravel, clay, etc.) with Netherite Shovel drops.

---

### Explosion
**Hunger:** 15 · **Cooldown:** 10s · **Power:** 4 (blast radius)

Creates a **large explosion** centered on your position.  
You **survive** at exactly 1 HP and are not knocked back.  
Everything around you is not so lucky.

---

### Thunder
**Hunger:** 3 · **Cooldown:** 5s · **Power:** 3 lightning bolts

Calls down **3 lightning bolts** around your position, striking nearby enemies.

---

### String Shot
**Hunger:** 1 · **Cooldown:** 2s · **Power:** radius 1

Lays **cobwebs** in a 1-block radius around you (at foot and head level), slowing anyone who walks into them.

---

### Defog
**Hunger:** 2 · **Cooldown:** 5s · **Power:** radius 10

- Clears weather → **sunny**
- Removes **fire** and **cobwebs** in a radius
- Dispels your **negative status effects** (Blindness, Weakness, Poison, Wither, Hunger, Slowness, Mining Fatigue, Bad Luck)

---

### Crab Hammer
**Hunger:** 3 · **Cooldown:** 2s

A heavy mace swing: **deals ~8 hearts of damage** and knocks back all enemies within 3 blocks in front of you.
