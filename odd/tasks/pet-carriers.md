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

- [ ] T1 S1-S2: remove the six mobs from `pickup_excluded`; check each is an `OwnableEntity` with an owner when tamed (the horse family, cats, parrots) and that the pickup scan, effects and icons work on them (riding, a parrot on the shoulder, a sitting cat); make take-back win over the horse family's inventory while it carries items.
- [ ] T2 S3: count the chest of tamed chested horses for holder effects, with no cap; keep them out of the pickup scan.
- [ ] T3 S5: changelog.
- [ ] T4: build and both dedicated servers (rcon checks: a tamed horse picks up, a mule's chest counts).
- [ ] T5: fresh review.
- [ ] T6: user in-game test on Fabric and Forge.

Risk: item 6 (no automated tests).

## Log
