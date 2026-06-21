# HM Case

The **HM Case** is a crafteable item that consolidates all 34 learned HMs into a single hotbar slot.
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

The GUI is a **6-row chest** with two clearly separated sections (no spacer/header panes):

```
┌──────────────────────────────────────────────────────────────┐
│ [ 24 active HM slots — rows 0–2 ]                             │
│ ( empty divider band — no spacer items )                     │
│ [ 10 toggle HM slots — rows 4–5 ]                            │
└──────────────────────────────────────────────────────────────┘
```

### Icons

Every learned HM is shown as its actual **HM disc** — a TM-style disc (gold "HM" rim around a
type-coloured ring: crimson for actives, blue for toggles) with the ability's trigger item as the
centre symbol, so abilities are easy to tell apart at a glance.

- **Grey glass pane** — ability not yet learned. The tooltip does **not** reveal which Pokémon
  unlocks it — that's a wiki secret.
- **Disc icon** — ability learned.
- **Glowing icon** — currently active, currently ON, or currently being moved.

### Active HMs section

**Right-click** a learned active HM to set it as the **active** (quick-use) ability. Right-clicking
the already-active ability **deselects** it.

### Toggle HMs section

- **Green `✔ [ON]`** — toggle is enabled.
- **Grey `[OFF]`** — toggle is learned but disabled.

**Right-click** any toggle to flip its state.

---

## Reordering (drag & drop)

You can rearrange HMs within a section by dragging:

1. **Left-click** an HM — it lifts onto your cursor (it's now "moving").
2. **Left-click** any slot in the **same** section — the two swap places.

(Click the original slot, or anywhere outside the section, to cancel.) Ordering is per-GUI-session
(it resets to default when you reopen the Case). Active and toggle sections reorder independently.

---

## HM independence

Learning an HM via the disc is **completely independent** of the HM Case.
The Case is purely a management and quick-access tool — abilities are tracked per-player, not
per-Case item. You can use multiple HM Cases or none at all.
