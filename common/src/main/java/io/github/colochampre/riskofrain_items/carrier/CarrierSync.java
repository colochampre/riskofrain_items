package io.github.colochampre.riskofrain_items.carrier;

import dev.architectury.networking.NetworkManager;
import io.github.colochampre.riskofrain_items.mixin.ChunkMapAccessor;
import io.github.colochampre.riskofrain_items.mixin.TrackedEntityAccessor;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Server side of the carried items sync, the one system for every carrier. The packet holds the entity id and the
 * summary (total count per distinct item, in the order each item first appears): {@code varint entityId, varint size,
 * size x (identifier item, varint count)}. A packet with size 0 clears the entity on the client. It is sent to a
 * player that starts tracking a carrier with items, and to every tracking player when the items change.
 * Loader-neutral: the start tracking event is wired in each loader's entrypoint. Nothing here touches client classes.
 */
public final class CarrierSync {

  public static final ResourceLocation CARRIED_ITEMS_PACKET = new ResourceLocation("riskofrain_items", "carried_items");

  private CarrierSync() {
  }

  /** Loader hook: a player started tracking an entity. Only carriers with items send anything. */
  public static void onStartTracking(Entity entity, ServerPlayer player) {
    if (!(entity instanceof Mob mob)) {
      return;
    }
    CarriedInventory inventory = ((RoRItemCarrier) mob).riskofrain_items$getCarriedInventory();
    if (inventory == null || inventory.isEmpty()) {
      return;
    }
    NetworkManager.sendToPlayer(player, CARRIED_ITEMS_PACKET, write(mob, CarriedItems.summarize(inventory)));
  }

  /** Sends the summary to every player tracking the mob. A mob nobody tracks (still loading) sends nothing. */
  static void sendToTrackers(Mob mob, List<CarriedItems.Entry> summary) {
    if (!(mob.level() instanceof ServerLevel level)) {
      return;
    }
    Object tracked = ((ChunkMapAccessor) level.getChunkSource().chunkMap).riskofrain_items$getEntityMap().get(mob.getId());
    if (tracked == null) {
      return;
    }
    Set<ServerPlayerConnection> seenBy = ((TrackedEntityAccessor) tracked).riskofrain_items$getSeenBy();
    if (seenBy.isEmpty()) {
      return;
    }
    List<ServerPlayer> players = new ArrayList<>(seenBy.size());
    for (ServerPlayerConnection connection : seenBy) {
      players.add(connection.getPlayer());
    }
    NetworkManager.sendToPlayers(players, CARRIED_ITEMS_PACKET, write(mob, summary));
  }

  private static FriendlyByteBuf write(Mob mob, List<CarriedItems.Entry> summary) {
    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
    buf.writeVarInt(mob.getId());
    buf.writeVarInt(summary.size());
    for (CarriedItems.Entry entry : summary) {
      buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(entry.item()));
      buf.writeVarInt(entry.count());
    }
    return buf;
  }
}
