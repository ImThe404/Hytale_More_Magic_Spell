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
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.collision.BlockCollisionData;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemPrePhysicsSystem;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
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
    } else {
        // Gravity 
        // Step 1: Apply gravity to velocity
        Vector3d position = store.getComponent(ref, TransformComponent.getComponentType()).getPosition();
        Velocity velocity = store.getComponent(ref, Velocity.getComponentType());
        Box boundingBox = store.getComponent(ref, BoundingBox.getComponentType()).getBoundingBox();
        PhysicsValues values = store.getComponent(ref, PhysicsValues.getComponentType());
        ItemPrePhysicsSystem.applyGravity(dt, boundingBox, values, position, velocity);
        // Step 2: Calculate scaled velocity for this tick
        Vector3d scaledVelocity = new Vector3d();
        velocity.assignVelocityTo(scaledVelocity).scale(dt);
        // Step 3: Check for collisions with terrain
        CollisionResult collisionResult = new CollisionResult();
        if (CollisionModule.isBelowMovementThreshold(scaledVelocity)) {
            CollisionModule.findBlockCollisionsShortDistance(world, boundingBox, position, scaledVelocity, collisionResult);
        } else {
            CollisionModule.findBlockCollisionsIterative(world, boundingBox, position, scaledVelocity, true, collisionResult);
        }
        // Step 4: Handle collision results
        BlockCollisionData blockCollisionData = collisionResult.getFirstBlockCollision();
        if (blockCollisionData != null && blockCollisionData.collisionNormal.equals(Vector3d.UP)) {
            // Hit ground - stop falling and place at collision point
            velocity.setZero();
            position.assign(blockCollisionData.collisionPoint);
        } else {
            // No collision or collision from side/top - apply velocity to position
            position.add(scaledVelocity);
        }
        // Step 5: Remove entity if it falls too far below world
        if (position.getY() < -32.0) {
            commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
        }
    }
  }

  @Nonnull
  @Override
  public Query<EntityStore> getQuery() {
    return Query.and(this.petrifiedComponentType);
  }
  
}
