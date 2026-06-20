# Commands

## Player commands

All players can use these:

```
/dittohms use <ability>
```
Activates an ability you have learned (same as right-clicking its disc or using the HM Case).

```
/dittohms list
```
Displays all 30 abilities with your current learn status, toggle state, and configured hunger/cooldown.

```
/dittohms select <ability>
```
Sets the active ability in the HM Case that is currently in your hotbar.

---

## Operator commands

Require **permission level 2** (operator):

```
/dittohms give <ability> <player>
```
Gives the HM Disc for the specified ability to a player.

```
/dittohms config <ability> hunger <0–20>
/dittohms config <ability> cooldown <0–24000>
/dittohms config <ability> power <0–512>
```
Adjusts an ability's tuning. Changes persist in `config/cobblemon_ditto_hms.json`.

```
/dittohms config <ability> reset
```
Resets a single ability to its default values.

```
/dittohms config reset_all
```
Resets every ability to defaults.

---

## Ability IDs

| ID | Display name |
|---|---|
| `water_gun` | Water Gun |
| `leafage` | Leafage |
| `cut` | Cut |
| `rock_smash` | Rock Smash |
| `rototiller` | Rototiller |
| `jump` | Jump |
| `surf` | Surf |
| `camouflage` | Camouflage |
| `stockpile_water` | Stockpile Water |
| `strength` | Strength |
| `rollout` | Rollout |
| `glide` | Glide |
| `waterfall` | Waterfall |
| `magnet_rise` | Magnet Rise |
| `dive` | Dive |
| `flash` | Flash |
| `ember` | Ember |
| `bullet_seed` | Bullet Seed |
| `teleport` | Teleport |
| `fly` | Fly |
| `rain_dance` | Rain Dance |
| `sunny_day` | Sunny Day |
| `rock_climb` | Rock Climb |
| `rest` | Rest |
| `dig` | Dig |
| `explosion` | Explosion |
| `thunder` | Thunder |
| `string_shot` | String Shot |
| `defog` | Defog |
| `crab_hammer` | Crab Hammer |
