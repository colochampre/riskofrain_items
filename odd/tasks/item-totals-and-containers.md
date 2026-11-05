# Item totals and container counting

Objective: show each item's current total in its tooltip, like Bustling Fungus, and count mod items stored in containers that sit directly in the player's inventory.
Branch: `1.20.1-architectury` (its PR into `1.20.1` is still to open). Delivery: `single-pr`, as the rest of the branch. Push, PR and merge are the user's.
Test runner: none in the project. Checks are a javac compile of `common` (Gradle cannot reach Mojang on the work network), `./gradlew build` when online, and the user's in-game test on Fabric and Forge.

## Specs

- S1: "Se puede implementar el mismo total en el resumen del tooltip, como lo tiene bustling fungus?" Every item whose effect grows with the count shows a total line built from the same formula the effect uses, only on the client, only when the player has at least one.
- S2: "es posible detectar los items para aplicar la pasiva en objetos con inventario propio, como bundles o shulkers? A lo mejor incluso items con inventario de mods, como backpacks (en esta version todavia no estan los bundles)."
- S3: "Solo en inventarios inmediatamente en el inventario del jugador. Un inventario dentro de otro inventario ya no"
- S4 (assumption): container contents count for the mod items only. Gold for Golden Gun still counts only when carried directly.
- S5 (assumption): Forge reads a container stack through its item handler capability, which mod backpacks expose, and falls back to the stack's NBT (`BlockEntityTag.Items` for shulker boxes, `Items` for bundles). Fabric 1.20.1 has no item-in-item storage lookup (`ItemStorage` only has `SIDED`, for blocks), so Fabric reads the NBT only.

## Tasks

- [x] T1 S1 inline: formulas move to `ItemFormulas`, shared by effects and tooltips; one tooltip helper (`TotalTooltip`); total lines in en_us, es_es, es_mx. `52b1117`
- [x] T2 S2,S3,S4,S5 inline: one-level container counting in `ItemUtils.countItemStack`, platform part through `@ExpectPlatform` (`ContainerContents`). Bustling Fungus moves to the player tick. Commit below.
- [ ] T3 S1-S5 user: in-game test on Fabric and Forge.

Risk: item 6 (no automated test covers tooltips or counting).

## Log

- L1 (2026-11-05), user: "Todo correcto. Se puede implementar el mismo total en el resumen del tooltip, como lo tiene bustling fungus? Además, es posible detectar los items para aplicar la pasiva en objetos con inventario propio, como bundles o shulkers? A lo mejor incluso items con inventario de mods, como backpacks (en esta version todavia no estan los bundles). Solo en inventarios inmediatamente en el inventario del jugador. Un inventario dentro de otro inventario ya no"
- L2: findings before writing.
  - Fabric transfer API 4.0.6 (`fabric-transfer-api-v1-4.0.6`): `ItemStorage` exposes only `SIDED` (`BlockApiLookup`), so items inside an item cannot be looked up on Fabric 1.20.1.
  - Bundles exist in 1.20.1 only behind the experimental `bundle` feature; their stack NBT is `Items`, so reading it costs nothing.
  - On Forge, a mod backpack may keep its contents on the server only, so the client tooltip total can be lower than the server effect for those.
  - Route: inline for both tasks. Writer trigger does not hold: T2 depends on the counting helper T1 also touches, and each unit is about 200 lines.
- L3: T1 checks. javac of all `common` sources passes (28 classes). A scratch harness printed `ItemFormulas` for 1, 2, 4, 16 and 64 items; every value matches the inline formulas they replaced (Golden Gun left out of the harness because `Mth` needs the game bootstrap; its formula moved verbatim).
- L4: T2 notes and checks.
  - `inventoryTick` only runs for stacks in the player's own slots, so a fungus inside a shulker box would never heal. Bustling Fungus moves to `PassiveItemEffects` on the player tick; its timer state is dropped when the player has no fungus or leaves the server.
  - javac passes for `common` (29 classes), the Fabric `ContainerContentsImpl` against the common jars, and the Forge `ContainerContentsImpl` against the Forge 47.4.3 named jar.
  - Not run: Gradle build and servers (Loom needs the Mojang manifest, blocked on the work network); real container reading needs the in-game test.
