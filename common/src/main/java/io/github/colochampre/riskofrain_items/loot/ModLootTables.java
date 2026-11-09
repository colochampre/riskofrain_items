package io.github.colochampre.riskofrain_items.loot;

import dev.architectury.event.events.common.LootEvent;
import io.github.colochampre.riskofrain_items.utils.ItemUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModLootTables {
  private static final Set<ResourceLocation> SMALL_CHEST_TABLES = chests("abandoned_mineshaft", "ancient_city",
          "bastion_treasure", "buried_treasure", "desert_pyramid", "end_city_treasure", "igloo_chest", "jungle_temple",
          "nether_bridge", "pillager_outpost", "ruined_portal", "shipwreck_supply", "shipwreck_treasure", "simple_dungeon",
          "stronghold_corridor", "stronghold_crossing", "stronghold_library", "underwater_ruin_big",
          "underwater_ruin_small", "woodland_mansion");

  private static final Set<ResourceLocation> LARGE_CHEST_TABLES = chests("bastion_treasure", "buried_treasure",
          "end_city_treasure", "jungle_temple", "shipwreck_treasure", "stronghold_library");

  private static Set<ResourceLocation> chests(String... names) {
    return Stream.of(names).map(name -> new ResourceLocation("chests/" + name)).collect(Collectors.toSet());
  }

  public static void init() {
    LootEvent.MODIFY_LOOT_TABLE.register((lootDataManager, id, context, builtin) -> {
      if (SMALL_CHEST_TABLES.contains(id)) {
        context.addPool(smallChestPool());
      }
      if (LARGE_CHEST_TABLES.contains(id)) {
        context.addPool(largeChestPool());
      }
    });
  }

  // 50% chance of 1-2 items, with the small chest odds of ItemUtils: common 80%, uncommon 20%
  private static LootPool.Builder smallChestPool() {
    List<Item> commonItems = ItemUtils.getCommonItems();
    List<Item> uncommonItems = ItemUtils.getUncommonItems();
    LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1))
            .when(LootItemRandomChanceCondition.randomChance(0.5F));
    // The weights cancel out each list's size, so all common items together weigh 4 times all uncommon items
    commonItems.forEach(item -> pool.add(oneOrTwo(item, 4 * uncommonItems.size())));
    uncommonItems.forEach(item -> pool.add(oneOrTwo(item, commonItems.size())));
    return pool;
  }

  // Always 1 item, with the large chest odds of ItemUtils: uncommon 100%
  private static LootPool.Builder largeChestPool() {
    LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1));
    ItemUtils.getUncommonItems().forEach(item -> pool.add(LootItem.lootTableItem(item)));
    return pool;
  }

  private static LootItem.Builder<?> oneOrTwo(Item item, int weight) {
    return LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)));
  }
}
