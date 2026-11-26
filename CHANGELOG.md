# Changelog

## Unreleased - Minecraft 1.20.1 (Fabric & Forge)

### New
- **Mobs pick up Risk of Rain items.** Mobs can pick up Risk of Rain items from the ground and gain their passive effects. Four groups, each behind its own setting and all off by default: vanilla hostile mobs, pets (tamed wolves and other mobs with an owner), neutral creatures (wild wolves, polar bears, iron golems and neutral mobs from other mods) and hostile mobs from other mods. Pets include horses (also skeleton and zombie horses), camels, cats and parrots; for donkeys, mules and llamas, the Risk of Rain items in their chest count instead. Creepers, bosses and mobs that fly or split are excluded.
- **Carried items drop on death.** Optionally, mobs carrying items never despawn, so the items are not lost. A mob that converts into another (a zombie into a drowned, a skeleton into a stray) keeps its items; if the new mob cannot carry them, they drop.
- **Take your items back.** The owner of a tamed mob (for example a wolf) can take its carried items back by sneaking and right-clicking it with an empty hand. Iron golems and other mobs without an owner never give items back.
- **Carried items above the head.** Carriers show the icons of their items, with counts, within a configurable distance.
- **Data-pack tags.** `riskofrain_items:pickup_excluded` removes mobs from every group; `riskofrain_items:pickup_allowed` lets other-mod mobs pick up items even if they are not hostile.
- **Config screen** (Cloth Config) through Mod Menu on Fabric and the mods list on Forge: groups, carried slots, no-despawn, and icon display.
- **Carrier API for other mods.** Other mods can register their own carrier rules so their mobs pick up items, and a render scale for mobs whose renderer scales the model so the icons sit right above them.

### Changed
- **Cloth Config is now a required dependency** on both Fabric and Forge.

## 2.0.0 - Minecraft 1.20.1 (Fabric & Forge)

### New
- **Fabric support.** The mod now runs on both Fabric and Forge (requires Architectury API; Fabric API on Fabric).
- **Item totals in tooltips.** Every item that scales with stacks shows its current total (for example "Total: 155% damage (4 in inventory)"), so you don't have to do the math.
- **Items work from containers.** Items inside shulker boxes, bundles and mod backpacks (e.g. Sophisticated Backpacks on Forge) count toward their effects, as long as the container is directly in your inventory. Containers inside containers don't count. Golden Gun only counts gold you carry directly.
- **Goat Hoof field of view.** The zoom-out from speed is capped at the equivalent of +40%, so stacking hooves no longer stretches the view. Your actual speed is unaffected.

### Balance
- **All items count your whole inventory.** One stack of 64 now works exactly like 8 stacks of 8.
- **Goat Hoof:** +14% movement speed per hoof while sprinting (was a Speed potion effect).
- **Soldier's Syringe:** +15% attack speed per syringe (was a Haste potion effect). It no longer speeds up mining.
- **Rose Buckler:** while sprinting, reduces incoming damage using the Risk of Rain 2 armor curve (was a Resistance potion effect): 23.1% with 1, 37.5% with 2, 60% with 5, 75% with 10. It never reaches 100%. Damage that ignores resistance (like void or `/kill`) is not reduced.
- **Bustling Fungus:** heals 5% of max health (+2.25% per extra fungus) every second after standing still for 2 seconds (was 5% +1%, capped at 10%, every 2 seconds after 4 seconds). Healing is no longer rounded down to whole points.
- **Lens-Maker's Glasses:** 6.25% critical chance per item, matching the tooltip (it was only ~1.5% per item).
- **Infusion:** the max health you earn is lost on death (it can be earned again). It is kept when leaving the End.

### Fixes
- Shipwreck supply chests now generate items.
- Repulsion Armor Plate no longer raises very small hits up to 1 damage.
- Damage items no longer trigger on hits that armor or absorption already reduced to 0.
- Smart Shopper no longer triggers on mobs that give no experience.
- Infusion no longer piles up one health modifier per kill.
- Items in the offhand and armor slots now count for every item.
- Chances were 1% lower than stated; they now match the tooltip.

## 1.0 - Minecraft 1.20.1 (Forge)

- First release (built from the former `riskofrainmod` project).
