package io.github.colochampre.riskofrain_items.client;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.networking.NetworkManager;
import io.github.colochampre.riskofrain_items.carrier.CarrierSync;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client side of the carried items sync: receives the summaries sent by {@link CarrierSync} and keeps them by entity
 * id for {@link CarriedItemsRenderer}. The store belongs to one level: it is emptied when that level is replaced,
 * when the player joins, respawns or leaves, and each entry goes when its entity is removed (see ClientLevelMixin).
 * Client only: loaded from the client side initialization, never on a dedicated server.
 */
@Environment(EnvType.CLIENT)
public final class CarriedItemsClient {

  private static final Map<Integer, List<CarriedItemsRenderer.Carried>> STORE = new HashMap<>();
  private static ClientLevel storeLevel;

  private CarriedItemsClient() {
  }

  public static void init() {
    NetworkManager.registerReceiver(NetworkManager.Side.S2C, CarrierSync.CARRIED_ITEMS_PACKET, (buf, context) -> {
      int entityId = buf.readVarInt();
      List<CarriedItemsRenderer.Carried> items = read(buf);
      context.queue(() -> put(entityId, items));
    });
    ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> clear());
    ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> clear());
    ClientPlayerEvent.CLIENT_PLAYER_RESPAWN.register((oldPlayer, newPlayer) -> clear());
  }

  private static List<CarriedItemsRenderer.Carried> read(FriendlyByteBuf buf) {
    int size = buf.readVarInt();
    List<CarriedItemsRenderer.Carried> items = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      ResourceLocation id = buf.readResourceLocation();
      int count = buf.readVarInt();
      // An item whose mod was removed is skipped rather than drawn as air
      Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
      if (item != null && count > 0) {
        items.add(new CarriedItemsRenderer.Carried(new ItemStack(item), count > 1 ? "x" + count : null));
      }
    }
    return items;
  }

  private static void put(int entityId, List<CarriedItemsRenderer.Carried> items) {
    ClientLevel level = Minecraft.getInstance().level;
    if (level == null) {
      return;
    }
    if (level != storeLevel) {
      STORE.clear();
      storeLevel = level;
    }
    if (items.isEmpty()) {
      STORE.remove(entityId);
    } else {
      STORE.put(entityId, items);
    }
  }

  /** The icons to draw for the entity, empty for anything that carries nothing. */
  static List<CarriedItemsRenderer.Carried> get(Entity entity) {
    if (STORE.isEmpty() || entity.level() != storeLevel) {
      return List.of();
    }
    return STORE.getOrDefault(entity.getId(), List.of());
  }

  /** Called when the client level drops an entity. */
  public static void remove(int entityId) {
    STORE.remove(entityId);
  }

  private static void clear() {
    STORE.clear();
    storeLevel = null;
  }
}
