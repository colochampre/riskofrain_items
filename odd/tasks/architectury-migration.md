# Architectury migration

Objective: move the Risk of Rain items mod from `riskofrainmod` (`items_1.20.1`, Forge only) into this repo with the Architectury layout used by `riskofrain_mobs`.
Branch: `1.20.1-architectury` into `1.20.1`. Delivery: `single-pr` (initial import, nothing to review against). Push, PR and merge are the user's.
Test runner: none in the project. Checks are `./gradlew build --offline`, dedicated server start on both loaders, loot checks from the server console, and the user's in-game test.

## Specs

- S1: "llevarnos todo lo que corresponda del proyecto riskofrainmod y su branch items_1.20.1 a la rama 1.20.1 (main) en github.com/colochampre/riskofrain_items.git"
- S2: "armarla con la estructura de Architectury como con la que venimos trabajando en riskofrain_mobs" (`common`, `fabric`, `forge`, same versions and class layout as `riskofrain_mobs` 1.20.1).
- S3 (assumption): existing worlds keep working, so the mod id, every registry id and all translation keys stay the same.
- S4 (assumption): item behavior is the same on Forge as before, and matches on Fabric.

## Tasks

- [x] T1 S1,S2 inline: Gradle project, metadata, entry points, docs. `ecbb819`
- [x] T2 S1,S2,S3 inline: registries, items, block, resources. `9bbee3d`
- [x] T3 S2,S4 inline: item effects in common, damage hook per platform. `cf510ca`
- [x] T4 S2,S3,S4 inline: chest loot in common, replacing the Forge loot modifiers. `591bb72`
- [ ] T5 S1-S4 inline: build (done), server start on both loaders and loot check from the console (pending EULA authorization for `fabric/run` and `forge/run`).
- [ ] T6 S4 user: in-game test on Fabric and Forge.

Risk: item 6 (no automated test would catch a regression in the damage hook or the loot pools). Item 5 does not apply: the build copies the versions `riskofrain_mobs` already uses.

## Log

- L1 (2026-11-02), user: "Anteriormente el mod tenía los mobs actuales, sus ítems de huevo y torreta, y además ítems que spawneaban por estructuras del mundo. Ese mod fue dividido en dos a pedido de la comunidad, a partir de riskofrainmod, quedó mobs e items divididos por ramas. Para lo siguiente que quiero hacer, precisaría tener un repo propio de riskofrain_items independiente, para mantener y actualizar con comodidad. Habría que llevarnos todo lo que corresponda del proyecto riskofrainmod y su branch items_1.20.1 a la rama 1.20.1 (main) en github.com/colochampre/riskofrain_items.git pero armarla con la estructura de Architectury como con la que venimos trabajando en riskofrain_mobs"
- L2: decisions from reading the source.
  - History starts fresh, as `riskofrain_mobs` did. `riskofrainmod` keeps the old commits.
  - Version `1.2.0`: the last Gradle version of the branch, above the `1.1` in its `mods.toml`.
  - No Cloth Config, Mod Menu or client entry point: this mod has no config and no client code.
  - Damage: Fabric API has no way to change damage (`ServerLivingEntityEvents.ALLOW_DAMAGE` can only cancel) and neither has Architectury, whose `LIVING_HURT` also runs before armor. Forge keeps `LivingDamageEvent`, Fabric gets a mixin at the same point, and both call common code. A single common mixin is not safe: Forge rewrites `actuallyHurt`.
  - `Player` overrides `actuallyHurt`, so the Fabric mixin targets both `LivingEntity` and `Player`. Effects on the victim (Repulsion Armor Plate, Rose Buckler, Tougher Times) need the `Player` one.
  - Loot: global loot modifiers are Forge only. `LootEvent.MODIFY_LOOT_TABLE` adds pools on both loaders. Weights reproduce the old odds: 50% chance of 1-2 items from a 80/20 common/uncommon split, and always 1 uncommon item in the 6 large-chest tables.
  - `minecraft:chests/shipwreck_suply` in the old loot modifier is a typo: that table does not exist, so shipwreck supply chests never got items. Fixed to `shipwreck_supply`.
  - `SmallChestBlockEntity` and the block entity registry are fully commented-out work in progress. They move as they are, Forge imports included.
  - `add_*_chance_items.json` loot tables are not referenced anywhere. They move as they are.
  - Known difference: Forge stops `actuallyHurt` when the raw damage is 0, vanilla does not. A player hitting a mob with a 0-damage projectile (snowball, egg) can play proc sounds on Fabric. Player victims are not affected, because `Player.hurt` already rejects 0 damage.
- L3: checks so far, on `591bb72`.
  - `./gradlew build --offline`: passes. `9bbee3d` and `cf510ca` also compile on both loaders.
  - All 51 item, block, painting and sound ids match the old mod. The only ids removed are the 3 loot modifier serializers.
  - All 102 kept resources are byte-identical. The only ones removed are the 3 Forge loot modifier files.
  - Item classes differ only in the renamed `RoRItems` and `RoRSounds`.
  - The common loot table list matches the old one except for the `shipwreck_supply` fix.
  - The Fabric jar has the mixin config and a refmap mapping `actuallyHurt` to `method_6074`. The Forge jar has no mixins.
  - `LOAD ordinal = 6` read from the bytecode of `LivingEntity` and `Player` in 1.20.1: the 7th load of the amount in each is the `amount != 0` check. Not yet proven at runtime.
  - Next: start both servers, confirm the mixin applies, and count injected items with console `loot` commands.
