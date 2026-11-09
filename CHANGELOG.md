# Changelog

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
