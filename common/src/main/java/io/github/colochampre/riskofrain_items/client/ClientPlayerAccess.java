package io.github.colochampre.riskofrain_items.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Client-only access to the local player. Kept in its own class so common code that runs on a dedicated
 * server never loads {@link Minecraft}; call it only from client-side paths such as tooltips.
 */
@Environment(EnvType.CLIENT)
public final class ClientPlayerAccess {
  private ClientPlayerAccess() {
  }

  public static @Nullable Player localPlayer() {
    return Minecraft.getInstance().player;
  }
}
