package org.MoreMagicSpell.Systems;

import javax.annotation.Nonnull;
import org.MoreMagicSpell.Components.StoneWallComponent;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class StoneWallSystem extends EntityTickingSystem<EntityStore> {

  private final ComponentType<EntityStore, StoneWallComponent> stoneWallComponentType;

  private static final HytaleLogger LOGGER = HytaleLogger.getLogger();

  public StoneWallSystem(ComponentType<EntityStore, StoneWallComponent> stoneWallComponentType) {
    this.stoneWallComponentType = stoneWallComponentType;
  }

  @Override
  public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

    // Get StoneWallComponent and Ref
    StoneWallComponent stoneWall = archetypeChunk.getComponent(index, stoneWallComponentType);
    Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
    World world = commandBuffer.getExternalData().getWorld();

    if (stoneWall.isExpired()) {

        // Remove all Invisible Blocks
        if (stoneWall.getInvisibleBlocks() != null) {
          for (Vector3i blockPos : stoneWall.getInvisibleBlocks()) {
              world.breakBlock(blockPos.x, blockPos.y, blockPos.z, 0);
          }
        }

        // Remove StoneWallComponent
        commandBuffer.removeComponent(ref, stoneWallComponentType);

        // Remove StoneWall entity after a short delay to allow for any despawn animations
        world.execute(() -> {   
            commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
        });
        
        LOGGER.atInfo().log("StoneWall entity expired: " + ref.toString());
    } 
  }

  @Nonnull
  @Override
  public Query<EntityStore> getQuery() {
    return Query.and(this.stoneWallComponentType);
  }
  
}
