# Risk of Rain Items

A Minecraft mod that brings items from Risk of Rain into Minecraft. Keep them in your inventory and their effects apply on their own, stacking with each copy.

Items, paintings and sounds used to live in [riskofrainmod](https://github.com/colochampre/riskofrainmod). Mobs are now in [riskofrain_mobs](https://github.com/colochampre/riskofrain_mobs).

## Features

- Common items: Bustling Fungus, Crowbar, Goat Hoof, Lens-Maker's Glasses, Repulsion Armor Plate, Soldier's Syringe, Topaz Brooch, Tougher Times
- Uncommon items: Golden Gun, Infusion, Rose Buckler, Smart Shopper
- Items found in structure chests
- Risk of Rain paintings
- Built with Architectury API for Fabric and Forge

## Requirements

Minecraft 1.20.1 with [Architectury API](https://www.curseforge.com/minecraft/mc-mods/architectury-api/files/all?version=1.20.1), plus:

- **Fabric:** Fabric Loader and [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api/files/all?version=1.20.1)
- **Forge:** Forge 47

## Development

`common` holds the shared code, and `fabric` and `forge` hold what each platform needs. Each Minecraft version has its own branch.

```shell
./gradlew build             # Jars end up in fabric/build/libs and forge/build/libs
./gradlew :fabric:runClient
./gradlew :forge:runClient
```

## License

[MIT](LICENSE.txt)
