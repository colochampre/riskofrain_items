# Mob item pickup (items side)

Objective: let mobs that carry Risk of Rain items get the same passive effects a player gets, so riskofrain_mobs enemies can pick items up from the ground and use them. This document covers the riskofrain_items side; the mobs side (inventory, pickup, config) has its own document in riskofrain_mobs.
Branch: `1.20.1-mob-item-pickup` from `1.20.1` (`6e33c72`). Delivery: ask-on-risk. Push, PR and merge are the user's.
Test runner: none in the project. Checks are `./gradlew build` (online on the home machine), `:fabric:runServer` / `:forge:runServer` stopped through rcon, and the user's in-game test on Fabric and Forge.

## Specs

User request (2026-11-14): "se podría hacer que los mobs de riskofrain_mobs tengan la capacidad de acumular items de riskofrain_items como lo hacen los aldeanos granjeros? La idea es que se beneficien de las pasivas de los items al igual que un jugador."

- S1: No hard dependency in either direction. riskofrain_items never references riskofrain_mobs classes; riskofrain_mobs never references riskofrain_items classes. Both mods keep working alone.
- S2: A non-player entity counts as an item holder when it implements vanilla `InventoryCarrier` and is in the entity type tag `#riskofrain_items:item_users`. Its count is the total of each item in `getInventory()` (no nested containers). Players keep the current counting (inventory plus one level of containers, S2/S3 of item-totals-and-containers).
- S3: riskofrain_items defines the item tag `#riskofrain_items:passive_items` with every Risk of Rain item, so other mods can pick them up by tag id. The entity tag ships empty; riskofrain_mobs adds its own entities to it from its own data pack (`data/riskofrain_items/tags/entity_types/item_users.json`).
- S4: "Con infusion, golden gun y smart shopper, y cualquier item que no aplique a mobs, simplemente se que les permita juntar, pero sin efectos." These three stay player only. They are still in `passive_items`, so mobs pick them up.
- S5: These items work on an item holder with the same formula as on a player: Crowbar, Lens Maker's Glasses, Topaz Brooch (on the holder's kills), Repulsion Armor Plate, Tougher Times, Bustling Fungus, plus Goat Hoof and Rose Buckler with the S6 trigger.
- S6: "Adaptar el disparador de Goat hoof solo para mobs. El jugador debe seguir teniendo su velocidad normal a menos que esprinte. Lo mismo con Rose buckler." On a holder, Goat Hoof and Rose Buckler trigger while it moves (horizontal position changed since its previous tick); on a player they keep triggering only while sprinting.
- S6b: "En cuanto a Syringe, que no haga nada en mobs. Rompería animaciónes timeadas para que coincidan con el sonido." Soldier's Syringe is player only, like the S4 items.
- S7: Effects run only on the server. Bustling Fungus keeps a timer per entity UUID; the state of a holder is dropped when it dies or is removed, so the map does not grow.
- S8 (assumption): no count cache. A holder's inventory is small (8 slots by default in riskofrain_mobs), so counting it every tick costs about the same as a player's.

## Tasks

- [ ] T1 S1-S3: `ItemUtils.countItemStack(LivingEntity, Item)` with the player and holder paths; `isItemHolder(LivingEntity)`; the two tag files.
- [ ] T2 S4-S7: `ModEvents` and `PassiveItemEffects` take any `LivingEntity` that is a player or a holder (except the S4 and S6b items); holder tick hook through Architectury; moving check for holders (S6), sharing the per-entity position state with Bustling Fungus; state cleanup.
- [ ] T3: build, both servers, and a console check: summon a vanilla `InventoryCarrier` (allay) added to the tag in a scratch data pack, give it items, confirm effects.
- [ ] T4: user in-game test, together with the riskofrain_mobs side.

Risk: item 6 (no automated test covers effects).

## Log

- L1 (2026-11-14), user: design agreed in conversation. Ground pickup only ("Por ahora solo que los puedan juntar del piso"); config lives in riskofrain_mobs: section "Item pickup", enemies on, drones off, 8 slots. "Artifact of Evolution" is reserved for a future spawn-with-items mode.
- L2: findings before writing. All effects filter through `ModEvents.serverPlayer` and `PassiveItemEffects.onPlayerTick`; the damage hook already receives any `LivingEntity` (Forge `LivingDamageEvent`, Fabric mixin on `LivingEntity` and `Player`), so no new mixin is needed for damage.
- L3 (2026-11-14), user resolved the sprint and syringe question (S6, S6b). Out of scope, for later in riskofrain_mobs: "Más adelante ajustamos además el momento en que se aplica el daño del hit de la animación".
