package io.github.colochampre.riskofrain_items.mixin;

import net.minecraft.server.network.ServerPlayerConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

// The players that currently track an entity, which vanilla keeps in ChunkMap.TrackedEntity.seenBy
@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public interface TrackedEntityAccessor {

  @Accessor("seenBy")
  Set<ServerPlayerConnection> riskofrain_items$getSeenBy();
}
