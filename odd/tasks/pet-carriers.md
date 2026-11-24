# Pet carriers

Objective: tamed pets that do not fight (horses, camels, cats, parrots, donkeys, mules, llamas) also benefit from Risk of Rain items, such as Goat Hoof, Bustling Fungus, Rose Buckler, Armor Plate or Tougher Times (backlog item 3c).
Branch: `1.20.1-pet-carriers` from `1.20.1` (`8b52fef`). Delivery: ask-on-risk. Push, PR and merge are the user's.
Test runner: none in the project. Checks are `./gradlew build`, dedicated servers stopped through rcon, and the user's in-game test on Fabric and Forge.

## Specs

User decisions (2026-11-23): "Luego voy a querer que otros mobs domesticables puedan juntar items también ... Y el jugador siempre puede volver a recuperarlos si quiere"; the two groups below ("De acuerdo con los grupos"); "que recuperar tenga prioridad mientras cargan algo".

- S1: Tamed horses, camels, skeleton horses, zombie horses, cats and parrots with an owner pick up and carry items like the other pets, under the existing pets setting (`PETS_PICK_UP`). They leave `#riskofrain_items:pickup_excluded`.
- S2: Owner take-back (sneak + right-click with an empty main hand) wins over vanilla while the mob carries items; this covers the horse family, whose vanilla sneak + right-click opens its inventory. With nothing carried, vanilla behavior is unchanged (the inventory opens, a cat or parrot sits). The inventory still opens with the inventory key while riding.
- S3: Tamed donkeys, mules, llamas and trader llamas with an owner do not pick up from the ground. Instead, the Risk of Rain items in their chest count for their holder effects, under the same pets setting. No cap: every item in the chest counts (user: "No le pondría un tope. Si el jugador quiere una mascota casi inmortal, que pueda hacerlo"); the cost is the items the player gives up. The chest is the take-back (vanilla UI), and its contents already drop on death.
- S4: The allay stays out (it picks up items with its own logic); every other exclusion is unchanged. A data pack can still exclude any mob through `pickup_excluded`.
- S5: The changelog "Unreleased" section mentions the new pets in one line.

## Tasks

- [x] T1 S1-S2: remove the six mobs from `pickup_excluded`; check each is an `OwnableEntity` with an owner when tamed (the horse family, cats, parrots) and that the pickup scan, effects and icons work on them (riding, a parrot on the shoulder, a sitting cat); make take-back win over the horse family's inventory while it carries items.
- [x] T2 S3: count the chest of tamed chested horses for holder effects, with no cap; keep them out of the pickup scan.
- [x] T3 S5: changelog (the unreleased pickup entry updated in place) and the `PETS_PICK_UP` tooltip in en_us, es_es and es_mx.
- [ ] T4: build and both dedicated servers (rcon checks: a tamed horse picks up, a mule's chest counts).
- [ ] T5: fresh review.
- [ ] T6: user in-game test on Fabric and Forge.

Risk: item 6 (no automated tests).

## Log

- L1 (2026-11-23) T1: removed horse, camel, skeleton_horse, zombie_horse, cat and parrot from `pickup_excluded` (donkey, mule, llama, trader_llama stay for T2). No Java change was needed.
  - Vanilla check (1.20.1 Mojang-mapped classes): `AbstractHorse` (horse, camel, skeleton and zombie horse) implements `OwnableEntity`, `Cat` and `Parrot` extend `TamableAnimal` (`OwnableEntity`). All read and write the `Owner` key (UUID int array, or a legacy name string). So the PETS group covers them once they have an owner. Trap skeleton horses (`SkeletonTrapGoal`) are `setTamed(true)` with no owner, and `Tame:1b` summons have none either: they are ownerless, fall to the vanilla group (not `Enemy`, not `NeutralMob`) and do not pick up. Only a horse tamed by a player (`tameWithName` sets the owner) carries. The allay is an `InventoryCarrier` and not `OwnableEntity`, so `canCarry` rejects it with no tag entry needed.
  - Take-back (S2): `Mob.interact` is final and our HEAD hook runs before `mobInteract`, so with items and the owner sneaking with an empty main hand, take-back wins over `AbstractHorse.mobInteract` (sneak opens the inventory only after it). With nothing carried `takeAll` returns false and vanilla runs unchanged (horse inventory, cat or parrot sit toggle). Sneaking while riding dismounts, so a rider cannot trigger it on another mob unexpectedly. A camel's second rider can take items back from the camel.
  - Ridden scan: `serverAiStep` runs for a ridden horse (`AbstractHorse.isImmobile` is only true while dead, eating or rearing; no `isEffectiveAi` override), so the scan keeps working; it pauses only while the horse eats or rears.
  - Parrot on a shoulder: `ShoulderRidingEntity.setEntityOnShoulder` stores `saveWithoutId` on the player and calls `discard()` (no death, no `checkDespawn`), so our drop paths do not run; our key is written by `addAdditionalSaveData` and `respawnEntityOnShoulder` reloads it through `readAdditionalSaveData`. Checked a `CarriedItems` round trip through summon NBT. While on the shoulder the parrot is not an entity, so its items give no holder effects until it hops off.
  - Goat Hoof: `PassiveItemEffects.onHolderTick` adds a transient `MOVEMENT_SPEED` modifier while the holder moves horizontally; `AbstractHorse.getRiddenSpeed` (and the camel's) reads `MOVEMENT_SPEED`, so a ridden horse gets the bonus. Not tested with a rider (needs a player).
  - Servers (rcon, `PETS_PICK_UP` on, restored after): Fabric and Forge both: tamed and owned horse, camel, skeleton horse, zombie horse, cat and parrot picked up a crowbar and show `riskofrain_items:CarriedItems`; untamed horse, untamed cat, ownerless tamed skeleton horse, donkey and allay did not. `./gradlew build` passes.
  - Left: T2 must count the chest in `ItemUtils.countItemStack` (it does not depend on `canCarry`, only the pickup scan does, so keeping the chested horses in `pickup_excluded` does not block it). T6: take-back with sneak + right-click on horse, camel, cat and parrot, riding with Goat Hoof, shoulder parrot.
- L2 (2026-11-23) T2: tamed donkeys, mules and llamas (any `AbstractChestedHorse`) with a chest and an owner count the Risk of Rain items in their chest for holder effects, under `PETS_PICK_UP`, with no cap.
  - Design: `ItemUtils.isItemHolder` and `countItemStack` gain a third source next to `CarriedItems` and the `item_users` inventory. Guard `hasCountedChest`: `instanceof AbstractChestedHorse` (checked first, so other mobs pay one instanceof), `hasChest()`, `getOwnerUUID() != null`, `PETS_PICK_UP`. The holder check does not scan the container; the three counts of `onHolderTick` scan it (about 15 slots each) only for owned chested horses with the setting on. The container is read through the new accessor mixin `AbstractHorseAccessor` (protected `AbstractHorse.inventory`); the whole container is scanned (slots 0-1 are saddle and carpet, never RoR items). Every effect path already goes through these two methods, so Bustling Fungus, Goat Hoof, Rose Buckler, Armor Plate, Tougher Times and the rest apply as to any carrier.
  - Setting: the pets setting is the only switch; the donkey, mule and llama entries stay in `pickup_excluded`, which only blocks ground pickup for them (a data pack cannot turn off the chest effects through it). Icons: not synced for chested horses (the chest is visible in the vanilla UI); not a carrier, so no take-back code either.
  - NBT: 1.20.1 chest slots are `Slot:2b` to `Slot:16b` (the full container index; `Slot:0b` is ignored, it is the saddle slot).
  - Servers (rcon), Fabric and Forge, `PETS_PICK_UP` on (restored to false after): after 8 damage, an owned mule and an owned llama with a Bustling Fungus in the chest healed back to full (53); an owned mule without the item, an ownerless tamed mule with it and an owned mule with the item but no `ChestedHorse` stayed at vanilla regen values. With the setting off the owned mule and llama with the item did not heal. An owned chested mule did not pick up a crowbar from the ground (no `CarriedItems`, item still there). Test entities removed and chunks unforced. `./gradlew build` passes.
  - Left for T6: in-game check of the chest UI with real items, a mule with Goat Hoof under a rider, and that the effect stops when the owner removes the item.
