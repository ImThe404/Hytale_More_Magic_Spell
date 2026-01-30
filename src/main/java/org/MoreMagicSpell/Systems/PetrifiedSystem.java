package org.MoreMagicSpell.Systems;

import javax.annotation.Nonnull;
import org.MoreMagicSpell.Components.PetrifiedComponent;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PetrifiedSystem extends EntityTickingSystem<EntityStore> {

  private final ComponentType<EntityStore, PetrifiedComponent> petrifiedComponentType;

  private static final HytaleLogger LOGGER = HytaleLogger.getLogger();

  public PetrifiedSystem(ComponentType<EntityStore, PetrifiedComponent> petrifiedComponentType) {
    this.petrifiedComponentType = petrifiedComponentType;
  }

  @Override
  public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

    // Get PetrifiedComponent and Ref
    PetrifiedComponent petrifiedComponent = archetypeChunk.getComponent(index, petrifiedComponentType);
    Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
    World world = commandBuffer.getExternalData().getWorld();

    if (petrifiedComponent.isExpired()) {
        // Petrification duration has ended - restore original state
        
        // Restore original entity state from PetrifiedComponent's originalHolder
        Holder<EntityStore> originalHolder = petrifiedComponent.getOriginalHolder();
        if (originalHolder != null) {
            world.execute(() -> {   
                commandBuffer.addEntity(originalHolder, AddReason.LOAD);
            });
        } else {
            LOGGER.atWarning().log("Original holder is null for entity: " + ref.toString());
        }

        // Remove PetrifiedComponent
        commandBuffer.removeComponent(ref, petrifiedComponentType);

        // Remove brainless petrified entity
        world.execute(() -> {   
            commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
        });

        LOGGER.atInfo().log("StoneWall entity expired: " + ref.toString());
    }
  }

  @Nonnull
  @Override
  public Query<EntityStore> getQuery() {
    return Query.and(this.petrifiedComponentType);
  }
  
}
