# HM Case

The **HM Case** is a crafteable item that consolidates all 30 learned HMs into a single hotbar slot.  
It replaces the need to hold individual HM Discs and provides a management GUI.

---

## Crafting

Shapeless recipe (9 ingredients):

- 1× Chest
- 1× Diamond  
- 1× each of all 7 Apricorn colours (Red, Yellow, Green, Blue, Black, White, Pink)

---

## Usage

| Action | Result |
|---|---|
| **Right-click** | Activates the currently selected active HM (no item cooldown — always usable) |
| **Shift + right-click** | Opens the HM Case GUI |

The HM Case does **not** apply an item cooldown, so you can always open the GUI even when the active ability is cooling down.

---

## HM Case GUI

The GUI is a **4-row chest** with two distinct sections:

```
┌─────────────────────────────────────────────────────────────┐
│  [1] [2] [3] [4] [5] [6] [7] [8] [9]   ← Active HMs (row 0)│
│  [10][11][12][13][14][15][16][17][18]   ← Active HMs (row 1)│
│  [19][20][21][22][23][24][═══][═══][══] ← Active + separator│
│  [T1][T2][T3][T4][T5][T6][   ][   ][  ]← Toggle HMs (row 3)│
└─────────────────────────────────────────────────────────────┘
```

### Active HMs section (rows 0–2)

- **Grey glass pane** — ability not yet learned. Tooltip shows which Pokémon to interact with and what item to hold.
- **Coloured disc** — ability learned. Click to set as the **active** (quick-use) ability.
- **Glowing disc** ✦ — currently selected active ability.

Clicking an already-selected ability **deselects** it.

### Gold separator

Visually separates active HMs from toggle HMs.

### Toggle HMs section (row 3)

- **Grey glass pane** — not yet learned.
- **Green `[ON]`** — toggle is enabled.
- **Red `[OFF]`** — toggle is learned but disabled.

Click any toggle to flip its state.

---

## HM independence

Learning an HM via the disc is **completely independent** of the HM Case.  
The Case is purely a management and quick-access tool — abilities are tracked per-player, not per-Case item.  
You can use multiple HM Cases or none at all.
