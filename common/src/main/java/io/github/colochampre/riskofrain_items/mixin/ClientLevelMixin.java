package io.github.colochampre.riskofrain_items.mixin;

import io.github.colochampre.riskofrain_items.client.CarriedItemsClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Drops the synced carried items of an entity when the client level removes it (unloaded, killed, out of range). Client mixin
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

  @Inject(method = "removeEntity", at = @At("HEAD"))
  private void riskofrain_items$forgetCarriedItems(int entityId, Entity.RemovalReason reason, CallbackInfo ci) {
    // ClientLevel.addEntity calls removeEntity first, for an id that is not there yet. On Fabric the carried items
    // packet arrives before the entity (EntityTrackingEvents.START_TRACKING fires at the head of addPairing), so
    // only a removal of an entity that exists may forget its items
    if (((ClientLevel) (Object) this).getEntity(entityId) != null) {
      CarriedItemsClient.remove(entityId);
    }
  }
}
