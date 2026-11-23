# Carrier extension API

Objective: other mods (first riskofrain_mobs) can hand their mobs to the riskofrain_items carrier system with their own pickup rules, so riskofrain_mobs can drop its duplicate inventory, sync and icon code (backlog item 3b). Companion task: riskofrain_mobs `odd/tasks/use-items-carriers.md`.
Branch: `1.20.1-carrier-extension-api` from `1.20.1` (`8f91970`). Delivery: ask-on-risk. Push, PR and merge are the user's. This PR merges before the riskofrain_mobs one.
Test runner: none in the project. Checks are `./gradlew build`, dedicated servers stopped through rcon, and the user's in-game test on Fabric and Forge.

## Specs

User decisions (2026-11-16, 2026-11-23): "Si tomasemos la decision de unificarlos, convendría hacerlo antes del port y release"; one sync system for every carrier, no migration of old saves (L4 of `vanilla-mob-item-pickup.md`); riskofrain_mobs keeps working without riskofrain_items, as an optional dependency ("Dale, vamos con A").

- S1: A registration point where a mod gives the carrier rule for its own entity types: for a registered type the mod's rule alone decides whether a mob can pick up items, taking precedence over the `InventoryCarrier` exclusion, the four group settings, the boss tags and `pickup_allowed`. `pickup_excluded` still wins, so a data pack can always turn a mob off. Unregistered types keep the current rules. Called once at mod init; safe to call before or after riskofrain_items' own init.
- S2: Everything else already applies to registered mobs with no extra code: pickup scan, capacity (`CARRIED_SLOTS`), save, death drop, conversions, the despawn rule, owner take-back, holder effects and icons. `CarriedItems.dropAll(Mob)` stays public for mods whose mobs leave the world without dying (a drone breaking down, a turret picked up as an item).
- S3: Icons sit at the right height on mobs whose renderer scales the model (riskofrain_mobs Beetle, Gunner Turret and Wisp pass a render scale today; the dispatcher hook always uses 1). Fix it generically if possible (for example from the entity's real rendered height), otherwise through the registration point.
- S4: The changelog "Unreleased" section mentions the API in one line.

## Tasks

- [ ] T1 S1-S2: the registration point in `carrier/`, used first in `CarrierEligibility.canCarry`; Javadoc for mod authors.
- [ ] T2 S3: icon height on scaled renderers.
- [ ] T3 S4: changelog.
- [ ] T4: build and both dedicated servers start.
- [ ] T5: user in-game test together with the riskofrain_mobs task (its T7).

Risk: item 6 (no automated tests).

## Log
